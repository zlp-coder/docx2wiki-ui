package op.tools.docx2wiki_ui;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import op.tools.docx2wiki.DocTransfer;
import op.tools.docx2wiki.ExcelTransfer;
import op.tools.docx2wiki.WikiOperator;
import op.tools.docx2wiki_ui.controler.MainControler;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Properties;

public class WikiFileTransThread extends Thread {

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
    public TextArea txtWikiFileMsg;

    @FXML
    public TextField txtOutPath;



    public WikiFileTransThread(TextField ptxtWikiUrl , TextField ptxtWikiUser, PasswordField ptxtWikiPsw,
                               TextArea ptxtWikiFileMsg , List<File> pobjectFile , Properties pProp ,
                               File pfileProperties , TextField pTxtOutPath) {
        txtWikiUrl = ptxtWikiUrl;
        txtWikiUser = ptxtWikiUser;
        txtWikiPsw = ptxtWikiPsw;
        txtWikiFileMsg = ptxtWikiFileMsg;
        _objectFile = pobjectFile;
        _prop = pProp;
        _fileProperties = pfileProperties;
        txtOutPath = pTxtOutPath;
    }

    @Override
    public void run() {
        try {
            if (txtOutPath.getText().isEmpty()) {
                wikiFileMsg("需要配置输出目录");
                return;
            }

            if(_prop != null ) {
                _prop.setProperty("outpath",txtOutPath.getText());
                _prop.store(new FileOutputStream(_fileProperties), null);
            }


            for (File f : _objectFile) {
                if (f.exists() && f.canRead()) {
                    String suffix = f.getName().substring(f.getName().lastIndexOf(".") + 1);
                    suffix = suffix.toLowerCase();

                    if (suffix.equals("doc") || suffix.equals("docx")) {
                        wikiFileMsg("开始处理word文件:" + f.getName());

                        WikiOperator oprt = new WikiOperator();
                        DocTransfer trans = new DocTransfer();
                        trans.Transfer(f.getName(), new FileInputStream(f), oprt);
                        publicFileText(oprt);

                        wikiFileMsg("word文件:" + f.getName() + "处理完成");
                    }

                    if (suffix.equals("xls") || suffix.equals("xlsx")) {
                        wikiFileMsg("开始处理excel文件:" + f.getName());

                        WikiOperator oprt = new WikiOperator();
                        ExcelTransfer trans = new ExcelTransfer();
                        trans.Transfer(f.getName(), new FileInputStream(f), oprt);
                        publicFileText(oprt);

                        wikiFileMsg("excel文件:" + f.getName() + "处理完成");
                    }

                } else {
                    wikiFileMsg("文件异常：" + f.getName());
                }
            }


        } catch (Exception ex) {
            wikiFileMsg("error:" + ex.getMessage());
            log.error(ex.getMessage());
        }
    }

    private void wikiFileMsg(String strMsg) {
        if (txtWikiFileMsg.getText().isEmpty()) {
            txtWikiFileMsg.setText(strMsg);
        } else {
            txtWikiFileMsg.setText(txtWikiFileMsg.getText() + "\r\n" + strMsg);
        }
    }

    private Boolean publicFileText(WikiOperator op) {

        String outPath = txtOutPath.getText() + File.separator + op.get_title();

        try {

            File fpath = new File(outPath);
            if(!fpath.exists()){
                fpath.mkdirs();
            }

            op.saveBmpasFile(outPath);

            File ftxt = new File(outPath + File.separator + op.get_title() + ".txt");
            FileOutputStream fo = new FileOutputStream(ftxt);
            fo.write(op.get_text().getBytes());
            fo.flush();
            fo.close();

            return true;

        } catch (Exception ex) {
            log.error(ex.getMessage());
            wikiFileMsg("error:" + ex.getMessage());
            return false;
        }
    }

}
