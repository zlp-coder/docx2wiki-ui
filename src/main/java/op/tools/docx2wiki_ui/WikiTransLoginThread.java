package op.tools.docx2wiki_ui;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import op.tools.docx2wiki.*;
import op.tools.docx2wiki_ui.controler.MainControler;
import org.apache.http.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class WikiTransLoginThread extends Thread {

    private Logger log = LoggerFactory.getLogger(MainControler.class);

    private List<File> _objectFile;
    private String _wikiLoginToken = "";
    private Properties _prop;
    private Stage _stage;
    private File _fileProperties;
    private HttpClient _httpClient;
    private CookieStore _cookieStore;

    @FXML
    public TextField txtWikiUrl;

    @FXML
    public TextField txtWikiUser;

    @FXML
    public PasswordField txtWikiPsw;

    @FXML
    public TextArea txtWikiMsg;


    public WikiTransLoginThread(TextField ptxtWikiUrl , TextField ptxtWikiUser, PasswordField ptxtWikiPsw,
                                TextArea ptxtWikiMsg , List<File> pobjectFile , Properties pProp , File pfileProperties) {
        txtWikiUrl = ptxtWikiUrl;
        txtWikiUser = ptxtWikiUser;
        txtWikiPsw = ptxtWikiPsw;
        txtWikiMsg = ptxtWikiMsg;
        _objectFile = pobjectFile;
        _prop = pProp;
        _fileProperties = pfileProperties;
    }

    @Override
    public void run() {
        try {
            loginWiki(txtWikiUrl.getText(), txtWikiUser.getText(), txtWikiPsw.getText());

        } catch (Exception ex) {
            wikiMsg("error:" + ex.getMessage());
            log.error(ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void wikiMsg(String strMsg)  {

        try {
            if (txtWikiMsg.getText().isEmpty()) {
                txtWikiMsg.setText(strMsg);
            } else {
                txtWikiMsg.setText(txtWikiMsg.getText() + "\r\n" + strMsg);
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

    private Boolean loginWiki(String wikiUrl, String userName, String pwd) {

        if(_wikiLoginToken != null && _wikiLoginToken.isEmpty() == false){
            return true;
        }

        if (wikiUrl == null || wikiUrl.isEmpty() || userName == null || userName.isEmpty() || pwd == null || pwd.isEmpty()) {
            log.error(String.format("paramater error. Loging wiki fail. wikiUral=%s userName = %s pwd = %s"
                    , wikiUrl, userName, pwd));
            wikiMsg("登录wiki失败，请确认使用了正确的用户名称和密码。");
            return false;
        }

        try {
            List<NameValuePair> parameters = new ArrayList<NameValuePair>();
            parameters.add(new BasicNameValuePair("action", "login"));
            parameters.add(new BasicNameValuePair("format", "xml"));
            parameters.add(new BasicNameValuePair("lgname", userName));
            parameters.add(new BasicNameValuePair("lgpassword", pwd));

            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(parameters, Consts.UTF_8);
            String lr = sendPost(wikiUrl + "/api.php",formEntity);

            Document document = DocumentHelper.parseText(lr);
            Element xmlr = document.getRootElement().element("login");

            if (xmlr.attribute("result").getValue().equals("NeedToken")) {

                parameters.add(new BasicNameValuePair("lgtoken", xmlr.attribute("token").getValue()));
                formEntity = new UrlEncodedFormEntity(parameters, Consts.UTF_8);
                lr = sendPost(wikiUrl + "/api.php", formEntity);

                lr = sendGet(wikiUrl + "/api.php", "action=tokens&format=xml" );
                document = DocumentHelper.parseText(lr);
                xmlr = document.getRootElement().element("tokens");

                _wikiLoginToken = xmlr.attribute("edittoken").getValue();

                if(_prop != null ) {
                    _prop.setProperty("wikiurl",txtWikiUrl.getText());
                    _prop.setProperty("wikiuser",txtWikiUser.getText());
                    _prop.setProperty("wikipsw",txtWikiPsw.getText());

                    //_prop.setProperty("outpath",txtOutPath.getText());
                    _prop.store(new FileOutputStream(_fileProperties), null);
                }

                wikiMsg("loging wiki sucess. edit toke = " + _wikiLoginToken);
                return true;
            } else {
                wikiMsg("loging wiki fail.");
                return false;
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return false;
        }
    }

    private Boolean publicText(WikiOperator op) {

        String wikiUrl = txtWikiUrl.getText();
        String userName = txtWikiUser.getText();
        String pwd = txtWikiPsw.getText();

        try {
            if (loginWiki(wikiUrl, userName, pwd) == false) {
                wikiMsg("发布到wiki失败");
                return false;
            }

            op.saveBmpasFile(new UIPathTools().getPath() + File.separator + "temp" + File.separator);

            for (UploadBmpInfo info : op.get_bmpInfo()) {
                File file = new File(info.get_FileFullName());
                FileBody bin = new FileBody(file);

                HttpEntity reqEntity = MultipartEntityBuilder.create()
                        .addPart("action", new StringBody("upload", ContentType.TEXT_PLAIN))
                        .addPart("format", new StringBody("xml", ContentType.TEXT_PLAIN))
                        .addPart("filename", new StringBody(info.get_FileName(), ContentType.TEXT_PLAIN))
                        .addPart("comment", new StringBody(info.get_FileName(), ContentType.TEXT_PLAIN))
                        .addPart("ignorewarnings", new StringBody("true", ContentType.TEXT_PLAIN))

                        .addPart("file", bin)
                        .addPart("token", new StringBody(_wikiLoginToken, ContentType.TEXT_PLAIN))
                        .build();

                String lrBmp = sendPost(wikiUrl + "/api.php", reqEntity);

                wikiMsg(lrBmp);
            }


            List<NameValuePair> parameters = new ArrayList<NameValuePair>();
            parameters.add(new BasicNameValuePair("action", "edit"));
            parameters.add(new BasicNameValuePair("format", "xml"));
            parameters.add(new BasicNameValuePair("title", op.get_title()));
            parameters.add(new BasicNameValuePair("text", op.get_text()));
            parameters.add(new BasicNameValuePair("token", _wikiLoginToken));

            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(parameters, Consts.UTF_8);

            String lr = sendPost(wikiUrl + "/api.php", formEntity);
            wikiMsg(lr);

            return true;

        } catch (Exception ex) {
            log.error(ex.getMessage());
            wikiMsg("error:" + ex.getMessage());
            return false;
        }
    }

    public String sendPost(String url, HttpEntity formEntity) {
        try {
            PrintWriter out = null;
            BufferedReader in = null;
            String result = "";

            if (_httpClient == null) {
                _httpClient = createHttpClient();
            }

            HttpClientContext context = HttpClientContext.create();

            HttpPost httpPost = new HttpPost(url);

            httpPost.setEntity(formEntity);

            CloseableHttpResponse response = null;
            response = (CloseableHttpResponse)_httpClient.execute(httpPost,context);

            String lr ="";
            HttpEntity responseEntity = response.getEntity();

            if (responseEntity != null) {
                lr= EntityUtils.toString(responseEntity);
                log.info("响应内容:" + lr);
            }

            return lr;
        } catch (ParseException | IOException e) {
            e.printStackTrace();
            return "";
        }

    }

    public String sendGet(String url, String param) {
        try {
            PrintWriter out = null;
            BufferedReader in = null;
            String result = "";

            if (_httpClient == null) {
                _httpClient = createHttpClient();
                _httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.IGNORE_COOKIES);
            }

            HttpClientContext context = HttpClientContext.create();
            HttpGet httpPost = new HttpGet(url + "?" + param );
            CloseableHttpResponse response = null;
            response = (CloseableHttpResponse)_httpClient.execute(httpPost,context);
            HttpEntity responseEntity = response.getEntity();

            for  (Cookie c : context.getCookieStore().getCookies()) {
                System.out.println(c.getName() +  ": "  + c.getValue());
            }

            if (responseEntity != null) {
                String lr = EntityUtils.toString(responseEntity);
                log.info("响应内容:" + lr);
                return lr;
            }

            return "";
        } catch (ParseException | IOException e) {
            e.printStackTrace();
            return "";
        }

    }

    private HttpClient createHttpClient(){

        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
        HttpProtocolParams.setUseExpectContinue(params, true);

        SchemeRegistry schReg = new SchemeRegistry();
        schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(),80));
        schReg.register(new Scheme("https",PlainSocketFactory.getSocketFactory(),433));
        ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params,schReg);

        CookieStore cookieStore = new BasicCookieStore();
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build();

        return httpClient;

    };
/*


 */
}
