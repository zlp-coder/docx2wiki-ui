package op.tools.docx2wiki_ui.controler;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import op.tools.docx2wiki_ui.UIPathTools;
import op.tools.docx2wiki_ui.WikiFileTransThread;
import op.tools.docx2wiki_ui.WikiTransLoginThread;
import op.tools.docx2wiki_ui.WikiTransThread;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;


public class MainControler implements Initializable {

    private Logger log = LoggerFactory.getLogger(MainControler.class);

    private List<File> _objectFile;
    private String _wikiLoginToken = "";
    private Properties _prop;
    private Stage _stage;
    private File _fileProperties;
    private HttpClient _httpClient;
    private CookieStore _cookieStore;

    @FXML
    public TextArea txtFiles;

    @FXML
    public TextField txtWikiUrl;

    @FXML
    public TextField txtWikiUser;

    @FXML
    public PasswordField txtWikiPsw;

    @FXML
    public TextArea txtWikiMsg;

    @FXML
    public TextField txtOutPath;

    @FXML
    public TextArea txtWikiFileMsg;

    @FXML
    public AnchorPane root;

    public MainControler() {
        _objectFile = new ArrayList<File>();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {

            _fileProperties = new File(new UIPathTools().getPath() + File.separator + "docx2wikiui.properties");

            wikiMsg(new UIPathTools().getPath());

            if(_fileProperties.exists() == false){
                _fileProperties.createNewFile();
            }

            if (_fileProperties.exists()) {

                if(_prop ==null){
                    _prop = new Properties();
                }

                _prop.load(new FileInputStream(_fileProperties));

                txtWikiUrl.setText(_prop.getProperty("wikiurl"));
                txtWikiUser.setText(_prop.getProperty("wikiuser"));
                txtWikiPsw.setText(_prop.getProperty("wikipsw"));

                txtOutPath.setText(_prop.getProperty("outpath"));
            }
        }catch (Exception ex){
            log.error(ex.getMessage());
        }
    }

    @FXML
    public void lbtSelPathClicked() {
        try {
            Stage stage = (Stage) txtFiles.getScene().getWindow();

            DirectoryChooser dChooser = new DirectoryChooser();

            dChooser.setTitle("please select out path");
            File f = dChooser.showDialog(stage);

            txtOutPath.setText(f.getAbsolutePath());

            if (_prop != null) {
                _prop.setProperty("outpath", txtOutPath.getText());
                _prop.store(new FileOutputStream(_fileProperties), null);
            }
        }catch (Exception ex){
            log.error(ex.getMessage());
        }

    }

    @FXML
    public void lbSelfilesClicked() {
        Stage stage = (Stage) txtFiles.getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("docx Files",
                        "*.docx")
        );

        if( _prop.getProperty("lastpath")!= null  && !_prop.getProperty("lastpath").isEmpty() ) {
            fileChooser.setInitialDirectory( new File(_prop.getProperty("lastpath")));
        }


        List<File> sel = fileChooser.showOpenMultipleDialog(stage);
        if (sel != null && sel.size() > 0) {
            txtFiles.setText("");

            for (File f : sel) {
                if (txtFiles.getText().isEmpty()) {
                    txtFiles.setText(f.getAbsolutePath());
                } else {
                    txtFiles.setText(txtFiles.getText() + "\r\n" + f.getAbsolutePath());
                }

                _objectFile.add(f);
            }

            try {
                _prop.setProperty("lastpath", sel.get(0).getParentFile().getCanonicalPath());
                _prop.store(new FileOutputStream(_fileProperties), null);
            }catch (Exception ex){
                ex.printStackTrace();
                log.error(ex.getMessage());
            }

        }
    }

    @FXML
    public void lbWikiConClicked() {
        if (txtWikiUrl.getText()==null || txtWikiUrl.getText().isEmpty()) {
            wikiMsg("没有找到wiki链接地址");
            return;
        }

        if (txtWikiPsw.getText() == null || txtWikiUser.getText() == null||
                txtWikiUser.getText().isEmpty() || txtWikiPsw.getText().isEmpty()) {
            wikiMsg("需要Wiki的用户名称和密码");
            return;
        }

        new WikiTransLoginThread(txtWikiUrl ,txtWikiUser, txtWikiPsw,txtWikiMsg,_objectFile , _prop , _fileProperties).run();
    }

    @FXML
    public void lbWikiTransClicked() {
        new WikiTransThread(txtWikiUrl ,txtWikiUser, txtWikiPsw,txtWikiMsg,_objectFile , _prop , _fileProperties).run();
    }

    @FXML
    public void lbWikiTransToFileClicked() {
        new WikiFileTransThread(txtWikiUrl ,txtWikiUser, txtWikiPsw,txtWikiFileMsg,_objectFile , _prop ,
                _fileProperties, txtOutPath).run();
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

    private void wikiFileMsg(String strMsg) {
        if (txtWikiFileMsg.getText().isEmpty()) {
            txtWikiFileMsg.setText(strMsg);
        } else {
            txtWikiFileMsg.setText(txtWikiFileMsg.getText() + "\r\n" + strMsg);
        }
        Thread.yield();
    }


}

//    public String sendPost(String url, String param) {
//        PrintWriter out = null;
//        BufferedReader in = null;
//        String result = "";
//        try {
//            if(_cookie == null){
//                _cookie = new CookieManager();
//            }
//            CookieHandler.setDefault(_cookie);
//
//            URL realUrl = new URL(url);
//            URLConnection conn = realUrl.openConnection();
//            conn.setRequestProperty("accept", "*/*");
//            conn.setRequestProperty("connection", "Keep-Alive");
//            conn.setRequestProperty("user-agent",
//                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
//            conn.setDoOutput(true);
//            conn.setDoInput(true);
//            out = new PrintWriter(conn.getOutputStream());
//            out.print(param);
//            out.flush();
//            in = new BufferedReader(
//                    new InputStreamReader(conn.getInputStream()));
//            String line;
//            while ((line = in.readLine()) != null) {
//                result += line;
//            }
//        } catch (Exception e) {
//            System.out.println("发送 POST 请求出现异常！" + e);
//            e.printStackTrace();
//        } finally {
//            try {
//                if (out != null) {
//                    out.close();
//                }
//                if (in != null) {
//                    in.close();
//                }
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
//        }
//        return result;
//    }


