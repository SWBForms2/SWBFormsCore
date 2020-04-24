<%-- 
    Document   : dataSourceEditor.jsp
    Created on : Feb 4, 2014, 4:33:43 PM
    Author     : javier.solis.g
--%><%@page import="java.util.Arrays"%><%@page import="org.semanticwb.datamanager.*"%><%@page import="java.io.*"%><%@page import="java.net.URLEncoder"%><%@page contentType="text/html" pageEncoding="UTF-8"%><%!
    byte[] readInputStream(InputStream in) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = in.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }   
        return baos.toByteArray();
    }
%><%
    String contextPath = request.getContextPath();   
    SWBScriptEngine eng = DataMgr.initPlatform("/admin/ds/admin.js", session);  
    if(!eng.hasUserRole("prog"))
    {
        response.sendError(403);
        return;
    }

    String id=request.getParameter("id"); 
    boolean iframe=request.getParameter("iframe")!=null; 
    
    if(iframe)
    {
%>
<section class="content-header">
    <h1>Archivos <small><%=id%></small>
    </h1>
    <ol class="breadcrumb">
        <li><a href="<%=contextPath%>/admin"><i class="fa fa-home"></i>Home</a></li>
        <li>Programación</li>
        <li class="active"><a href="prog_ds" data-history="#prog_ds" data-target=".content-wrapper" data-load="ajax">Archivos</a></li>        
    </ol>
</section>

<section id="content" style="padding: 7px">  
    <iframe class="ifram_content" src="<%=contextPath%>/admin/editor?id=<%=id%>" frameborder="0" width="100%">Cargando...</iframe>
    <script type="text/javascript">
        $(window).resize();
    </script>                        
</section><!-- /.content -->
<%
        return;
    }

    String dir = config.getServletContext().getRealPath("/") + "/" + request.getRequestURI().substring(1, request.getRequestURI().lastIndexOf("/")) + "/";

    if(id.equals("admin"))
    {
        dir=config.getServletContext().getRealPath("/")+"/admin/";
    }else if(id.equals("admin_jsp"))
    {
        dir=config.getServletContext().getRealPath("/")+"/admin/jsp/";
    }else if(id.equals("admin_ds"))
    {
        dir=config.getServletContext().getRealPath("/")+"/admin/ds/";
    }else if(id.equals("api"))
    {
        dir=config.getServletContext().getRealPath("/")+"/work/api/";
    }else if(id.equals("work"))
    {
        dir=config.getServletContext().getRealPath("/")+"/work/";
    }else if(id.equals("utils"))
    {
        dir=config.getServletContext().getRealPath("/")+"/utils/";
    }else if(id.equals("web-inf"))
    {
        dir=config.getServletContext().getRealPath("/")+"/WEB-INF/";
    }
    //System.out.println(dir);
    String sfn = request.getParameter("sfn");
    String filename = request.getParameter("fn");
    String ac=request.getParameter("ac");

    if(filename!=null && filename.length()==0)filename=null;

    if("ren".equals(ac))
    {
        File f=new File(dir+sfn);
        File fn=new File(dir+filename);
        if(f.exists() && !fn.exists())f.renameTo(fn);
    }

    if("del".equals(ac))
    {
        File f=new File(dir+sfn);
        if(f.exists())f.delete();
        filename=null;
    }
    
    String upload = request.getParameter("up");         
    if(upload!=null)
    {
        //System.out.println(upload);

        byte code[]=readInputStream(request.getInputStream());
        
        File fdir=new File(dir);
        fdir.mkdirs();

        try {
            FileOutputStream os = new FileOutputStream(dir+upload);
            os.write(code);
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }        
        
        out.print("OK");
        return;
    }
    
    boolean lint=false;
    String mode="text/html";
    if(filename!=null)
    {
        if(filename.endsWith(".js"))           
        {
            mode="text/javascript";
            lint=true;
        }else if(filename.endsWith(".json"))
        {
            mode="application/json";
            lint=true;
        }else if(filename.endsWith(".html"))
        {
            mode="text/html";
        }else if(filename.endsWith(".jsp"))
        {
            mode="application/x-jsp";
        }else if(filename.endsWith(".css"))
        {
            mode="text/css";
            lint=true;
        }else if(filename.endsWith(".xml"))
        {
            mode="text/xml";
        }else if(filename.endsWith(".rdf"))
        {
            mode="text/xml";
        }else if(filename.endsWith(".owl"))
        {
            mode="text/xml";
        }
    }
    
    
    String path = null;
    if (filename != null) {
        path = dir + filename;
    }
    
    String code="";

    if (path != null) {
        try {
            FileInputStream in = new FileInputStream(path);

            code = new String(readInputStream(in), "utf-8");

            //code=ITZFormsUtils.readInputStream(in,"utf-8");
            code = code.replace("<", "&lt;").replace(">", "&gt;");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //if(code==null)code="";
    //if(filename==null)filename="";
%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Editor</title>
        <link href="<%=contextPath%>/static/admin/bower_components/bootstrap/dist/css/bootstrap.min.css" rel="stylesheet" type="text/css" />          
        <link rel="stylesheet" href="<%=contextPath%>/static/plugins/bootstrap-select/css/bootstrap-select.min.css">
        
        <link rel="stylesheet" href="<%=contextPath%>/static/plugins/codemirror/lib/codemirror.css">
        <link rel="stylesheet" href="<%=contextPath%>/static/plugins/codemirror/addon/hint/show-hint.css">
        <link rel="stylesheet" href="<%=contextPath%>/static/plugins/codemirror/theme/eclipse.css">   
        <link rel="stylesheet" href="<%=contextPath%>/static/plugins/codemirror/addon/dialog/dialog.css">
        <link rel="stylesheet" href="<%=contextPath%>/static/plugins/codemirror/addon/lint/lint.css">  
        

        <script src="<%=contextPath%>/static/plugins/codemirror/lib/codemirror.js"></script>  
        <script src="<%=contextPath%>/static/plugins/codemirror/addon/hint/show-hint.js"></script>
        <script src="<%=contextPath%>/static/plugins/codemirror/addon/selection/active-line.js"></script> 
        <script src="<%=contextPath%>/static/plugins/codemirror/addon/lint/lint.js"></script>       
        <script src="<%=contextPath%>/static/plugins/codemirror/addon/search/search.js"></script> 
        <script src="<%=contextPath%>/static/plugins/codemirror/addon/search/searchcursor.js"></script>
        <script src="<%=contextPath%>/static/plugins/codemirror/addon/dialog/dialog.js"></script>
        
        
        <script src="<%=contextPath%>/static/plugins/codemirror/mode/xml/xml.js"></script>
        <script src="<%=contextPath%>/static/plugins/codemirror/addon/hint/xml-hint.js"></script>
        
        <script src="<%=contextPath%>/static/plugins/codemirror/mode/javascript/javascript.js"></script>
        <script src="<%=contextPath%>/static/plugins/codemirror/addon/hint/javascript-hint.js"></script>
        <script src="<%=contextPath%>/static/plugins/codemirror/addon/lint/javascript-lint.js"></script>
        <script src="//ajax.aspnetcdn.com/ajax/jshint/r07/jshint.js"></script>
        
        <script src="<%=contextPath%>/static/plugins/codemirror/addon/lint/json-lint.js"></script>
        <!--<script src="https://rawgithub.com/zaach/jsonlint/79b553fb65c192add9066da64043458981b3972b/lib/jsonlint.js"></script>-->
        
        <script src="<%=contextPath%>/static/plugins/codemirror/addon/edit/matchbrackets.js"></script>
        <script src="<%=contextPath%>/static/plugins/codemirror/addon/edit/closebrackets.js"></script>
        <script src="<%=contextPath%>/static/plugins/codemirror/addon/comment/continuecomment.js"></script>
        <script src="<%=contextPath%>/static/plugins/codemirror/addon/comment/comment.js"></script>
        
        <script src="<%=contextPath%>/static/plugins/codemirror/mode/css/css.js"></script>
        <script src="<%=contextPath%>/static/plugins/codemirror/addon/hint/css-hint.js"></script>
        <script src="<%=contextPath%>/static/plugins/codemirror/addon/lint/css-lint.js"></script>
        <!--<script src="https://rawgithub.com/stubbornella/csslint/master/release/csslint.js"></script>-->
        
        <script src="<%=contextPath%>/static/plugins/codemirror/mode/clike/clike.js"></script>
        
        <script src="<%=contextPath%>/static/plugins/codemirror/mode/htmlmixed/htmlmixed.js"></script>
        <script src="<%=contextPath%>/static/plugins/codemirror/mode/htmlembedded/htmlembedded.js"></script>
        <script src="<%=contextPath%>/static/plugins/codemirror/addon/hint/html-hint.js"></script>
        <script src="<%=contextPath%>/static/plugins/codemirror/addon/mode/multiplex.js"></script>
        <script src="<%=contextPath%>/static/plugins/codemirror/addon/fold/xml-fold.js"></script>
        <script src="<%=contextPath%>/static/plugins/codemirror/addon/edit/matchtags.js"></script>     
        
        <!-- jQuery 2.1.4 -->
        <script src="<%=contextPath%>/static/admin/bower_components/jquery/dist/jquery.min.js"></script>        
        <!-- Bootstrap 3.3.2 JS -->
        <script src="<%=contextPath%>/static/admin/bower_components/bootstrap/dist/js/bootstrap.min.js" type="text/javascript"></script>          
        <script src="<%=contextPath%>/static/plugins/bootstrap-select/js/bootstrap-select.min.js" type="text/javascript"></script>          
        
       <!--
        <sc´ript src="/static/plugins/codemirror/addon/edit/closetag.js"></script>
        -->
        <style type="text/css">
            .CodeMirror {border: 1px solid black; font-size:13px}
        </style>      
    </head>
    <body>
        <div style="padding:5px 0px 40px 10px;"><form action="" method="post" style="float: left;">   
            Archivo a Editar: 
            <input type="hidden" id="ac" name="ac">
            <input type="hidden" id="fn" name="fn">
            <select name="sfn" class="selectpicker" onchange="
                    if (value == '_new')
                        document.getElementById('fn').value = prompt('Archivo', '[Nombre del Archivo]');
                    else
                        document.getElementById('fn').value = value;
                    submit();
                    ">

                <%
                    if (filename == null) {
                        out.println("<option></option>");
                    }
                    String selected = "";
                    boolean fselected = false;
                    File d = new File(dir);
                    File[] files = d.listFiles();
                    Arrays.sort(files);
                    for (int x = 0; files!=null && x < files.length; x++) {
                        if (filename != null && filename.equals(files[x].getName())) {
                            selected = "selected";
                            fselected = true;
                        } else {
                            selected = "";
                        }
                        out.println("<option value=\"" + files[x].getName() + "\" " + selected + ">" + files[x].getName() + "</option>");
                    }
                    if (filename != null && fselected == false) {
                        out.println("<option value=\"" + filename + "\" selected>" + filename + "</option>");
                    }
                %>            
                <option value="_new">[New File]</option>
            </select>
<%                
                if(filename!=null && filename.length()>0)
                {
%>           
            &nbsp;<input type="button" class="btn btn-primary" value="Guardar" onclick="r=getSynchData('?id=<%=id%>&up=<%=filename!=null?URLEncoder.encode(filename):""%>',myCodeMirror.getValue(),'POST');console.log(r);if(r.response==='OK')alert('Archivo Gradado');else alert('Error al guardar archivo')">
            &nbsp;<input type="button" class="btn btn-info" value="Renombrar" onclick="
                var fname=prompt('Nuevo Nombre del Archivo', '<%=filename%>'); 
                if(fname!=null){ 
                    document.getElementById('fn').value=fname; 
                    document.getElementById('ac').value='ren';
                    submit();
                }
            ">
            &nbsp;<input type="button" class="btn btn-warning" value="Eliminar" onclick="
                var ret=confirm('Deseas eliminar el archivo <%=filename%>?'); 
                if(ret){ 
                    document.getElementById('ac').value='del';
                    submit();
                }                
            ">
<%
                }
%>
                
        </form></div>
              
            <%
                if (filename != null) {
            %>            
            <textarea name="code" id="code"><%=code.replace("<","&"+"lt;")%></textarea>           
            <script type="text/javascript">
                
                var getSynchData=function(url,data,method)
                {
                    if (typeof XMLHttpRequest === "undefined") 
                    {
                        XMLHttpRequest = function () {
                          try { return new ActiveXObject("Msxml2.XMLHTTP.6.0"); }
                          catch (e) {}
                          try { return new ActiveXObject("Msxml2.XMLHTTP.3.0"); }
                          catch (e) {}
                          try { return new ActiveXObject("Microsoft.XMLHTTP"); }
                          catch (e) {}
                          // Microsoft.XMLHTTP points to Msxml2.XMLHTTP and is redundant
                          throw new Error("This browser does not support XMLHttpRequest.");
                        };
                    }

                    var aRequest= new XMLHttpRequest();
                    if(!data)
                    {
                        if(!method)method="GET";
                        aRequest.open(method, url, false);
                        aRequest.send();
                    }else
                    {
                        if(!method)method="POST";
                        aRequest.open(method, url, false);
                        aRequest.send(data);
                    }
                    return aRequest;
                };                
                
                var myCodeMirror = CodeMirror.fromTextArea(code, {
                    //mode: "application/x-jsp",
                    mode: "<%=mode%>",
                    smartIndent: true,
                    lineNumbers: true,
                    styleActiveLine: true,
                    matchBrackets: true,
                    autoCloseBrackets: true,
                    theme: "eclipse",
                    continueComments: "Enter",
                    extraKeys: {"Ctrl-Space": "autocomplete","Ctrl-Q": "toggleComment","Ctrl-J": "toMatchingTag"},
                    matchTags: {bothTags: true},                  
                    <%if(lint){%>
                    gutters: ["CodeMirror-lint-markers"],
                    lint: true,
                    <%}%>
                });
                myCodeMirror.setSize("100%", parent.window.innerHeight-210);
            </script>      
            <%
                }
            %>            
    </body>
</html>
