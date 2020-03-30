<%-- 
    Document   : uml
    Created on : 17-mar-2018, 17:45:33
    Author     : javiersolis
--%>
<%@page import="java.util.HashSet"%>
<%@page import="java.util.Set"%>
<%@page import="org.semanticwb.datamanager.*"%><%@page contentType="text/html" pageEncoding="UTF-8"%><%!

%><%
    String contextPath = request.getContextPath();     
    SWBScriptEngine eng=DataMgr.initPlatform("/admin/ds/admin.js", session);    
    boolean iframe=request.getParameter("iframe")!=null;     
    //System.out.println(obj);
    String _title="Entity Diagram";
    String _smallName="";
    String _fileName=contextPath+"/admin/jsp/uml_entity.jsp";
    //if(!eng.hasUserAnyRole(obj.getDataList("roles_view")))response.sendError(403,"Acceso Restringido...");
    Set<String> setDS = new HashSet();
    setDS.add("visita");
    StringBuilder data=new StringBuilder();
    StringBuilder rel=new StringBuilder();
    if(iframe)
    {        
        DataObjectIterator it=eng.getDataSource("DataSource").find();
      	//int num=0;
        while (it.hasNext()) {
            DataObject obj = it.next();
            //num++;
            if(!obj.getBoolean("backend") && !obj.getBoolean("frontend"))continue;
            if(!setDS.contains(obj.getString("id"))) continue;
            data.append("[");
            data.append(obj.getString("id"));
            data.append("|");

            DataObject query=new DataObject();
            query.addSubList("sortBy").add("order");
            query.addSubObject("data").addParam("ds", obj.getId());
            DataObjectIterator it2=eng.getDataSource("DataSourceFields").find(query);
            //System.out.println("size:"+it2.size()+":"+it2.total());
            //if(num>220) break;
            while (it2.hasNext()) {
                DataObject fobj = it2.next();
                boolean req=fobj.getBoolean("required");
                String name=fobj.getString("name");
                String type=fobj.getString("type");
                String dataSource=null;
                String valueField=null;
                boolean multiple=false;

                if(req)data.append("*");
                data.append(name);

                query=new DataObject();
                query.addSubObject("data").addParam("dsfield", fobj.getId());
                DataObjectIterator it3=eng.getDataSource("DataSourceFieldsExt").find(query);
                while (it3.hasNext()) {
                    DataObject feobj = it3.next();
                    String att=feobj.getString("att");
                    String value=feobj.getString("value");   

                    if(att.equals("stype"))type+="("+value+")";
                    if(att.equals("dataSource"))
                    {
                        dataSource=value;
                        //if(valueField==null)valueField="_id";
                    }
                    if(att.equals("valueField"))valueField=value;                    
                    if(att.equals("multiple"))multiple=Boolean.parseBoolean(value);
                }

                if(dataSource!=null)
                {
                    data.append(":"+ dataSource+(valueField!=null?"."+valueField:""));                
                    rel.append("["+dataSource+"]");
                    if(multiple)
                    {
                        //data.append("(0..*)");
                        rel.append("*");
                    }
                    rel.append("<-");
                    rel.append("["+obj.getString("id")+"]");
                    rel.append("\\n");
                }     
                else if(type!=null)data.append(": "+type);     
                if(it2.hasNext())data.append(";");            
            }
            
            data.append("]\\n");        
        }
        data.append(rel);
    }
            
    //********************************** Ajax Content ************************************************************
    if(!iframe)
    {
%>
<!-- Content Header (Page header) -->
<section class="content-header">
    <h1>
        <%=_title%>
        <small><%=_smallName%></small>
    </h1>
    <ol class="breadcrumb">
        <li><a href="<%=contextPath%>/admin"><i class="fa fa-home"></i>Home</a></li>
        <li>Programación</li>
        <li class="active"><a href="<%=_fileName%>" data-history="#<%=_fileName%>" data-target=".content-wrapper" data-load="ajax"><%=_title%></a></li>
    </ol>
</section>
<!-- Main content -->
<section id="content" style="padding: 7px">  
    <iframe class="ifram_content" src="<%=_fileName%>?iframe=true" frameborder="0" width="100%"></iframe>
    <script type="text/javascript">
        $(window).resize();
    </script>            
</section>
<!-- /.content -->
<%
        //********************************** End Ajax ************************************************************
    }else
    {
%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>UML</title>
        <style>
            body{
                background-color: white;
            }
        </style>
    </head>
    <body>
        <script src="<%=contextPath%>/admin/utils/nomnoml/lodash.min.js"></script>
        <script src="<%=contextPath%>/admin/utils/nomnoml/dagre.min.js"></script>
        <script src="<%=contextPath%>/admin/utils/nomnoml/nomnoml.js"></script>

        <canvas id="target-canvas" style="height: 100%"></canvas>
        <script>
            var canvas = document.getElementById('target-canvas');
            var data="<%=data%>";
            nomnoml.draw(canvas, data);
        </script>   
    </body>
</html>
<%
    }
%>