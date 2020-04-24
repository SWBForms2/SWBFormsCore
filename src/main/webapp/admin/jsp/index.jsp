<%@page import="java.util.*"%><%@page import="org.semanticwb.datamanager.*"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%><%!

%><%
    String contextPath = request.getContextPath();
    SWBScriptEngine eng = DataMgr.initPlatform("/admin/ds/datasources.js", session);
    DataObject user = eng.getUser();
    String token=user.getString("token");
    
    DataObject q=new DataObject();
    q.addSubObject("data").addParam("user", user.getId()).addParam("readed", false);
    long noti=0;//eng.getDataSource("Notifications").find(q).size();
%><!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title><%=eng.getAppName()%> | Dashboard</title>
        <meta content='width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no' name='viewport'>
        <!-- Bootstrap 3.3.4 -->
        <link href="<%=contextPath%>/static/admin/bower_components/bootstrap/dist/css/bootstrap.min.css" rel="stylesheet" type="text/css" />    
        <link href="<%=contextPath%>/static/admin/bower_components/datatables.net-bs/css/dataTables.bootstrap.min.css" rel="stylesheet" type="text/css" />

        <!-- FontAwesome 4.5.0 -->
        <link href="<%=contextPath%>/static/admin/bower_components/font-awesome/css/font-awesome.min.css" rel="stylesheet" type="text/css" />
        <link rel="stylesheet" href="<%=contextPath%>/static/plugins/fontawesome-iconpicker-1.3.1/css/fontawesome-iconpicker.min.css">
        <!-- Ionicons 2.0.0 -->
        <link href="<%=contextPath%>/static/admin/bower_components/Ionicons/css/ionicons.min.css" rel="stylesheet" type="text/css" />   
        <!-- DataTables -->
        <link rel="stylesheet" href="<%=contextPath%>/static/admin/bower_components/datatables.net-bs/css/dataTables.bootstrap.min.css">        
              <!-- Theme style -->
        <link href="<%=contextPath%>/static/admin/dist/css/AdminLTE.min.css" rel="stylesheet" type="text/css" />
        <!-- AdminLTE Skins. Choose a skin from the css/skins 
             folder instead of downloading all of them to reduce the load. -->
        <link href="<%=contextPath%>/static/admin/dist/css/skins/_all-skins.min.css" rel="stylesheet" type="text/css" />
        <!-- Pace style -->
        <link href="<%=contextPath%>/static/admin/plugins/pace/pace.min.css" rel="stylesheet" type="text/css" >        
        <!-- iCheck -->
        <link href="<%=contextPath%>/static/admin/plugins/iCheck/flat/blue.css" rel="stylesheet" type="text/css" />
        <!-- Morris chart -->
        <!--        
                <link href="<%=contextPath%>/static/plugins/morris/morris.css" rel="stylesheet" type="text/css" />
        -->
        <!-- jvectormap -->
        <link href="<%=contextPath%>/static/admin/plugins/jvectormap/jquery-jvectormap-1.2.2.css" rel="stylesheet" type="text/css" />
        <!-- Date Picker -->
        <link href="<%=contextPath%>/static/admin/bower_components/bootstrap-datepicker/dist/css/bootstrap-datepicker.min.css" rel="stylesheet" type="text/css" />
        <!-- Daterange picker -->
        <link href="<%=contextPath%>/static/admin/bower_components/bootstrap-daterangepicker/daterangepicker.css" rel="stylesheet" type="text/css" />
        <!-- bootstrap wysihtml5 - text editor -->
        <link href="<%=contextPath%>/static/admin/plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.min.css" rel="stylesheet" type="text/css" />

        <link href="<%=contextPath%>/static/plugins/bootstrap-switch/bootstrap-switch.min.css" rel="stylesheet" type="text/css" />
        
        <link href="<%=contextPath%>/static/admin/plugins/iCheck/square/blue.css" rel="stylesheet" type="text/css" />

        <link rel="stylesheet" href="<%=contextPath%>/static/plugins/bootstrap-select/css/bootstrap-select.min.css">
        
        <!-- FileInputPlugin -- >
        <link href="<%=contextPath%>/css/fileinput.min.css" media="all" rel="stylesheet" type="text/css" />
        -->
<!--        
        <link rel="stylesheet" href="<%=contextPath%>/static/plugins/codemirror/lib/codemirror.css"> 
        <link rel="stylesheet" href="<%=contextPath%>/static/plugins/codemirror/addon/hint/show-hint.css">
        <link rel="stylesheet" href="<%=contextPath%>/static/plugins/codemirror/theme/eclipse.css">   
        <link rel="stylesheet" href="<%=contextPath%>/static/plugins/codemirror/addon/dialog/dialog.css">
        <link rel="stylesheet" href="<%=contextPath%>/static/plugins/codemirror/addon/lint/lint.css">         
-->
        <!-- Tabulator -->
        <link href="<%=contextPath%>/static/plugins/tabulator/css/tabulator.min.css" rel="stylesheet">
        <link href="<%=contextPath%>/static/plugins/tabulator/css/bootstrap/tabulator_bootstrap.min.css" rel="stylesheet">

        <style type="text/css">
            .CodeMirror {border: 1px solid black; font-size:13px}

            @media (min-width: 768px) {
                .main-header .sidebar-toggle {
                    display: none;
                }
                .main_alert
                {
                    padding-left: 250px
                }
                .nicheButtons{
                    position:absolute;
                }
            }
            
            .sidebar a {
                color: #b8c7ce;
            }
            
            .ifram_content{
                overflow:hidden;
                border:none;    
            }
            
            .tab_filled{
                border-top-color: #06ad58 !important;
            }
            
            .active{
                border-top-color: #3c8dbc !important;
            }
            
            @media (max-width: 767px)
            {
                .small-box {
                    text-align:unset !important;
                }         
                
                .small-box .icon {
                    display:unset !important;
                }

                .small-box p {
                    font-size:unset !important;
                }                
            }
        </style>   
        <!-- Cloudino Styles-->
        <style>
            .cdino_console{
                background-color: #00c0ef;
                min-height: 20px;
                margin-bottom: 10px;
                color: white;
                padding: 10px;
                font-family: courier;
                font-size: 13px;                            
            }
            .tab-pane{
                margin: 10px;
            }
            
            .cdino_buttons{
                padding-top: 10px;
            }
            
            .cdino_control {
                height: 90px;   
                display: inline-block;
            }
            
            .cdino_control_image {
                display: table;
            }    
            
            .cdino_text_menu{
                overflow:hidden;
                text-overflow:ellipsis;
            }
            
            dd{
                padding: 0px 0px 10px 0px;
            }
            
            .cdino_control_image, .cdino_control{
                padding: 10px 5px;
                margin: 0 0 10px 10px;
                min-width: 90px;
                font-size: 14px;
                border-radius: 3px;
                position: relative;
                padding: 15px 5px;
                text-align: center;
                color: #666;
                border: 1px solid #ddd;
                background-color: #f4f4f4;
                -webkit-box-shadow: none;
                box-shadow: none;                
                font-weight: 400;
                line-height: 1.42857143;
                white-space: nowrap;
                vertical-align: middle;
                -ms-touch-action: manipulation;
                touch-action: manipulation;
                cursor: pointer;
                -webkit-user-select: none;
                -moz-user-select: none;
                -ms-user-select: none;
                user-select: none;
            }        
        </style>        
        <script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.4.0/Chart.min.js"></script>
    </head>
    <body class="<%=eng.getConfigData().getString("appSkin","skin-green")%> sidebar-mini">
        <div class="wrapper">
            <header class="main-header">
                <!-- Logo -->
                <a href="<%=contextPath%>/admin/" class="logo">
                    <!--<img src="/img/cloudino.svg" width="170">-->
                    <!-- mini logo for sidebar mini 50x50 pixels 
                    <span class="logo-mini"><i class="fa fa-cloud"></i></span>-->
                    <!-- logo for regular state and mobile devices -->
                    <span class="logo-lg"><img style="background-color: white; border-radius: 50%; padding: 2px; width: 35px" src="<%=contextPath%>/admin/img/logo2.png" width="30"> <b><%=eng.getAppName()%></b></span>
                </a>
                
                <!-- Header Navbar: style can be found in header.less -->
                <nav class="navbar navbar-static-top" role="navigation">
                    <!-- Sidebar toggle button-->

                    <!-- Sidebar toggle button-->
                    <a href="#" class="sidebar-toggle" data-toggle="push-menu" role="button">
                        <span class="sr-only">Toggle navigation</span>
                    </a>

                    <div class="navbar-custom-menu">
                        <ul class="nav navbar-nav">
                            <!-- User Account: style can be found in dropdown.less -->                            
                            <li class="">
                                <a href="#" class="">
                                    <i class="fa fa-bell-o"></i>
                                    <span class="label label-warning"><%=noti%></span>
                                </a>
                            </li>                             
                            <li class="dropdown user user-menu">
                                <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                                    <img src="<%=contextPath%>/admin/img/user.jpg" class="user-image" alt="User Image"/>
                                    <span class="hidden-xs"><%=user.getString("fullname")%></span>
                                </a>
                                <ul class="dropdown-menu">
                                    <!-- User image -->
                                    <li class="user-header">
                                        <img src="<%=contextPath%>/admin/img/user.jpg" class="img-circle" alt="User Image" />
                                        <p>
                                            <%=user.getString("fullname")%>
                                            <small>Miembro desde: <%=user.getDateFormated("created",new Date(),"d MMMM yyyy")%></small>
                                        </p>
                                    </li>
                                    <!-- Menu Body -->
                                    <!--<li class="user-body">
                                      <div class="col-xs-4 text-center">
                                        <a href="#">Followers</a>
                                      </div>
                                      <div class="col-xs-4 text-center">
                                        <a href="#">Sales</a>
                                      </div>
                                      <div class="col-xs-4 text-center">
                                        <a href="#">Friends</a>
                                      </div>
                                    </li>-->
                                    <!-- Menu Footer-->
                                    <li class="user-footer">
                                        <div class="pull-left">
                                            <a href="<%=contextPath%>/admin/profile" data-history="#profile" class="btn btn-default btn-flat" data-target=".content-wrapper" data-load="ajax">Perfil</a>
                                        </div>
                                        <div class="pull-right">
                                            <a href="<%=contextPath%>/login?logout=true" class="btn btn-default btn-flat">Salir</a>
                                        </div>
                                    </li>
                                </ul>
                            </li>
                            
                            <!-- Control Sidebar Toggle Button -->
                            <%if(eng.hasUserRole("prog")){%>
                            <li class="logo" style="width:50px; padding: 0px; font-size: 16px">
                                <a href="#" id="code_button" data-slide_="false" data-toggle="control-sidebar"><i class="fa fa-code" style="font-weight: bold;"></i></a>
                            </li>     
                            <%}%>
                        </ul>
                    </div>
                </nav>
            </header>    

            <aside class="main-sidebar">
                <%@include file="admin_menu.jsp"%>
            </aside>                                

            <div id="alert_main" class="alert alert-success main_alert" style="display:none">
                <button type="button" class="close" onclick="$(this.parentElement).hide(500);" aria-hidden="true">×</button>
                <h4><i id="alert_icon" class="icon fa fa-check"></i> <span id="alert_title">Alert</span></h4>
                <span id="alert_content">Success alert preview. This alert is dismissable.</span>                    
            </div>

            <!-- Content Wrapper. Contains page content -->
            <div class="content-wrapper">
            </div><!-- /.content-wrapper -->
            <footer class="main-footer hidden-xs">
                <div class="pull-right hidden-xs">
                    <b>Version</b> 0.1
                </div>
                <strong>Copyright &copy; 2015-2020 <a href="<%=eng.getConfigData().getString("appURL","#")%>"><%=eng.getAppName()%></a>.</strong> All rights reserved.
            </footer>

            <!-- Add the sidebar's background. This div must be placed
                 immediately after the control sidebar -->
            <%if(eng.hasUserRole("prog")){%>
            <aside class="control-sidebar control-sidebar<%=(eng.getConfigData().getString("appSkin","skin-green").endsWith("-light")?"":"-dark")%>">
                <!-- Create the tabs -->
                <ul class="nav nav-tabs nav-justified control-sidebar-tabs">
                    <!--<li class="active"><a href="#prgmmer" data-toggle="tab"><i class="fa fa-home"></i> Programador</a></li>-->
                    <!--<li><a href="#cloudinojs" data-toggle="tab">CloudinoJS</a></li>-->
                </ul>
                <!-- Tab panes -->
                <div class="tab-content" style="padding: 1px 1px;">
                    <!-- Home tab content -->
                    <div class="tab-pane active" id="prgmmer">
                        <jsp:include page="prog_bar.jsp" />
                    </div><!-- /.tab-pane -->
                    <!-- Settings tab content --
                    <div class="tab-pane" id="cloudinojs">            
                        <jsp_:include page="cloudinojs.jsp" />
                    </div><!-- /.tab-pane -->                    
                </div>             
            </aside>
            <%}%>
        </div><!-- ./wrapper -->

        <!-- jQuery 2.1.4 -->
        <script src="<%=contextPath%>/static/admin/bower_components/jquery/dist/jquery.min.js"></script>
        
        <!-- Bootstrap 3.3.2 JS -->
        <script src="<%=contextPath%>/static/admin/bower_components/bootstrap/dist/js/bootstrap.min.js" type="text/javascript"></script>  
        <!-- PACE -->
        <script src="<%=contextPath%>/static/admin/bower_components/PACE/pace.min.js"></script>
        
        <!-- DataTables -->
        <script src="<%=contextPath%>/static/admin/bower_components/datatables.net/js/jquery.dataTables.min.js"></script>
        <script src="<%=contextPath%>/static/admin/bower_components/datatables.net-bs/js/dataTables.bootstrap.min.js"></script>

        <!-- jQuery Knob Chart -->
        <script src="<%=contextPath%>/static/admin/bower_components/jquery-knob/dist/jquery.knob.min.js" type="text/javascript"></script>

        <!-- AdminLTE App -->
        <script src="<%=contextPath%>/static/admin/dist/js/adminlte.min.js" type="text/javascript"></script>  
        
        <script src="<%=contextPath%>/static/plugins/validator/validator.min.js" type="text/javascript"></script>
        <script src="<%=contextPath%>/static/plugins/bootstrap-switch/bootstrap-switch.min.js" type="text/javascript"></script>  
        <script src="<%=contextPath%>/static/plugins/fontawesome-iconpicker-1.3.1/js/fontawesome-iconpicker.min.js" type="text/javascript"></script>  
        <!-- iCheck -->
        <script src="<%=contextPath%>/static/admin/plugins/iCheck/icheck.min.js" type="text/javascript"></script>
        
        <script src="<%=contextPath%>/static/admin/bower_components/bootstrap-waitingfor/build/bootstrap-waitingfor.js"></script>

        <!--        
                <script src="/js/fileinput.min.js"></script>
                <script src="/static/admin/bower_components/jquery-sparkline/dist/jquery.sparkline.min.js" type="text/javascript"></script>
                <script src="/static/admin/bower_components/moment/min/moment.min.js"></script>        
                <script src="/static/admin/bower_components/chart.js/Chart.min.js"></script>        
        -->
        <script src="<%=contextPath%>/admin/js/admin_utils.js"></script>
        <script type="text/javascript">
            $(document).ajaxStart(function () {Pace.restart()});
            $(document).ready(function(){
                    function fix_height()
                    {
                        var n=0;
                        //var n=$(".nav-tabs").height();
                        //if(n>0)n+=57;
                        var h = 161+n;
                        $(".ifram_content").attr("height", (($(window).height()) - h) + "px");
                    }
                    $(window).resize(function(){ fix_height(); });
                    //$("#preview").contentWindow.focus();
                    if(location.hash.length>0)loadContent("admin_content?p="+location.hash.substring(1),".content-wrapper");
                    else loadContent("home",".content-wrapper");
            });            
        </script>                 
        <script src="<%=contextPath%>/static/plugins/bootstrap-select/js/bootstrap-select.min.js" type="text/javascript"></script>  
        
        <!-- Tabulator 
        <script type="text/javascript" src="/static/plugins/tabulator/js/tabulator.min.js"></script>        
        -->
        
        <!-- Cloudino Scripts-->
<!--        
        <script type="text/javascript" src="/admin/js/websockets.js"></script>        
        <script src="https://maps.googleapis.com/maps/api/js?key=AIzaSyA5QIYs6gYTQ9EcdDG7iO2ksThk7L0M4QI&libraries=geometry,places"></script>  
-->
<%
    //out.println("token:"+token);
    if(token==null || token.isEmpty())
    {
%>
        <script type="text/javascript">
            //document.write("Android:"+(typeof Android));
            if(typeof Android != "undefined"){
                var token=Android.getToken();
                //document.write("token:"+token);
                if(token)
                {
                    $.get("<%=contextPath%>/login?token="+token);
                }
            }
        </script>
<%
    }
%>        
    </body>
</html>