package op.tools.docx2wiki_ui;


public class UIPathTools {
    public String getPath()
    {
        String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();

        if( System.getProperty("exe.path") != null){
            path = System.getProperty("exe.path");
            return path;
        }


        if(System.getProperty("os.name").contains("dows"))
        {
            path = path.substring(1,path.length());
        }

        if(path.contains("jar"))
        {
            path = path.replaceAll("ile:/","");
            path = path.substring(0,path.lastIndexOf("."));
            return path.substring(0,path.lastIndexOf("/"));
        }

        if(path.contains("war"))
        {
            path = path.substring(0,path.lastIndexOf("war"));
            path = path.substring(0,path.lastIndexOf("."));
            path = path.replaceAll("ile:/","");
            return path.substring(0,path.lastIndexOf("/"));
        }


        return path.replace("target/classes/", "");
    }
}
