package op.tools.docx2wiki_ui;

import op.tools.docx2wiki_ui.controler.MainControler;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) throws Exception {

        Logger log = LoggerFactory.getLogger(MainControler.class);

        CookieStore _cookieStore = new BasicCookieStore();
        String _wikiLoginToken;

//        _cookieStore.clear();
//        _cookieStore.addCookie(new BasicClientCookie("my_wiki_session", "f1ef7bf74bb969d7e0244d60ea3256f5"));
//        _cookieStore.addCookie(new BasicClientCookie("my_wikiUserID", "9"));
//        _cookieStore.addCookie(new BasicClientCookie("my_wikiUserName", "Zlp"));

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCookieStore(_cookieStore)
                .build();

        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(_cookieStore);

        HttpPost httpPost1 =
                new HttpPost("http://wiki.szedi.cn/api.php");
        //httpPost1.setHeader("Content-Type", "application/json;charset=utf8");

        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        //定义请求的参数  设置post参数
        parameters.add(new BasicNameValuePair("action", "login"));
        parameters.add(new BasicNameValuePair("format", "xml"));
        parameters.add(new BasicNameValuePair("lgname", "Zlp"));
        parameters.add(new BasicNameValuePair("lgpassword", "Moremoney2012"));

        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(parameters, Consts.UTF_8);

        httpPost1.setEntity(formEntity);

        CloseableHttpResponse response1 = null;
        response1 = (CloseableHttpResponse) httpClient.execute(httpPost1, context);
        HttpEntity responseEntity1 = response1.getEntity();
        for (Cookie c : context.getCookieStore().getCookies()) {
            System.out.println(c.getName() + ": " + c.getValue());
        }

        if (responseEntity1 != null) {
            String lr = EntityUtils.toString(responseEntity1);

            log.info("响应内容长度为:" + responseEntity1.getContentLength());
            log.info("响应内容:" + lr);

            Document document = DocumentHelper.parseText(lr);
            Element xmlr = document.getRootElement().element("login");

            //Pattern r = Pattern.compile("token=&quot;([0-9a-z]*?)&quot;");
            //Matcher m = r.matcher(lr);
            if (xmlr.attribute("result").getValue().equals("NeedToken")) {
                _wikiLoginToken = xmlr.attribute("token").getValue();
                log.info("loging wiki sucess. token=" + _wikiLoginToken);

                parameters.add(new BasicNameValuePair("lgtoken", _wikiLoginToken));

                formEntity = new UrlEncodedFormEntity(parameters, Consts.UTF_8);
                httpPost1.setEntity(formEntity);

                response1 = (CloseableHttpResponse) httpClient.execute(httpPost1, context);
                responseEntity1 = response1.getEntity();

                if (responseEntity1 != null) {
                    lr = EntityUtils.toString(responseEntity1);

                    log.info("响应内容长度为:" + responseEntity1.getContentLength());
                    log.info("响应内容:" + lr);
                }
            }
        }


//        CloseableHttpResponse response2 = (CloseableHttpResponse)httpClient.execute(httpPost1,context);
//        HttpEntity responseEntity2 = response2.getEntity();
//        for  (Cookie c : context.getCookieStore().getCookies()) {
//            System.out.println(c.getName() +  ": "  + c.getValue());
//        }
//
//        if (responseEntity2 != null) {
//            //log.info("响应内容长度为:" + responseEntity2.getContentLength());
//            String lr = EntityUtils.toString(responseEntity2);
//            log.info("响应内容:" + lr);
//        }

        HttpGet httpPost = new HttpGet( "http://wiki.szedi.cn/api.php?action=tokens&format=xml");
        //HttpGet httpPost = new HttpGet("http://wiki.szedi.cn/api.php?action=query&format=xml&meta=userinfo");


        CloseableHttpResponse response = null;
        response = (CloseableHttpResponse) httpClient.execute(httpPost, context);

        for (Cookie c : context.getCookieStore().getCookies()) {
            System.out.println(c.getName() + ": " + c.getValue());
        }


        HttpEntity responseEntity = response.getEntity();

        if (responseEntity != null) {
            String lr = EntityUtils.toString(responseEntity);

            System.out.println("响应内容长度为:" + responseEntity.getContentLength());
            System.out.println("响应内容:" + lr);
        }

        httpClient.close();

    }
}
