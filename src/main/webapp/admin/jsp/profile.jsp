<%-- 
    Document   : profile
    Created on : 27-mar-2018, 17:15:56
    Author     : javiersolis
--%><%@page import="org.semanticwb.datamanager.*"%><%@page contentType="text/html" pageEncoding="UTF-8"%><%
    String contextPath = request.getContextPath();     
    SWBScriptEngine eng = DataMgr.initPlatform("/admin/ds/datasources.js", session);
    DataObject user = eng.getUser();
    
    String fullname=request.getParameter("fullname");
    String email=request.getParameter("email");
    String password=request.getParameter("password");
    String password2=request.getParameter("password2");
    
    String msg=null;
    String style="";
    
    if(fullname!=null && email!=null && password!=null && password.equals(password2))
    {
        user.addParam("fullname", fullname);
        user.addParam("email", email);
        user.addParam("password", password);
        eng.getDataSource("User").updateObj(user);
        msg="Perfil actualizado...";   
        style="color: white;background: #00aa52;padding: 10px;";
    }else if(fullname!=null)
    {
        msg="Error al actualizar perfil...";
        style="color: white;background: #f03b2c;padding: 10px;";
    }
    
    //System.out.println(fullname);
%>
<!-- Content Header (Page header) -->
<section class="content-header">
    <h1>
        Perfil del usuario
    </h1>
    <ol class="breadcrumb">
        <li><a href="<%=contextPath%>/admin"><i class="fa fa-dashboard"></i> Home</a></li>
        <li class="active">Perfil</li>
    </ol>
</section>

<!-- Main content -->
<section class="content">

    <div class="row">
        <div class="col-md-3">

            <!-- Profile Image -->
            <div class="box box-primary">
                <div class="box-body box-profile">
                    <img class="profile-user-img img-responsive img-circle" src="<%=contextPath%>/admin/img/user.jpg" alt="User profile picture">
                    <h3 class="profile-username text-center"><%=user.getString("fullname")%></h3>
                    <!--<p class="text-muted text-center">Software Engineer</p>-->
                </div><!-- /.box-body -->
            </div><!-- /.box -->
        </div><!-- /.col -->
        <div class="col-md-9">
            <div class="nav-tabs-custom">
                <ul class="nav nav-tabs">
                    <li class="active"><a href="#settings" data-toggle="tab">Configuración</a></li>
                </ul>
                <div class="tab-content">
                    <div class="active tab-pane" id="settings">
                        <form class="form-horizontal" method="post" action="<%=contextPath%>/admin/profile" data-target=".content-wrapper" data-submit="ajax" role="form">
                            <div class="form-group">
                                <label for="fullname" class="col-sm-4 control-label">Nombre completo</label>
                                <div class="col-sm-8">
                                    <input type="text" class="form-control" name="fullname" id="fullname" placeholder="Nombre completo" value="<%=user.getString("fullname")%>">
                                </div>
                            </div>
                            <div class="form-group">
                                <label for="email" class="col-sm-4 control-label">Correo Electrónico</label>
                                <div class="col-sm-8">
                                    <input type="text" class="form-control" name="email" id="email" placeholder="Correo electrónico" value="<%=user.getString("email")%>">
                                </div>
                            </div>
                            <div class="form-group">
                                <label for="password" class="col-sm-4 control-label">Contraseña</label>
                                <div class="col-sm-8">
                                    <input type="password" name="password" id="inputPassword" data-minlength="4" class="form-control" placeholder="Contraseña" data-error="Requiere un tamañp minimo de 4 caracteres">
                                </div>
                                <div class="help-block with-errors col-sm-offset-4 col-sm-8"></div>
                            </div>
                            <div class="form-group">
                                <label for="password2" class="col-sm-4 control-label">Confirmar contraseña</label>
                                <div class="col-sm-8">
                                    <input type="password" name="password2" id="confirmPassword" data-match="#inputPassword" data-match-error="La contraseñas no coinciden" class="form-control" placeholder="Confirmar contraseña">
                                </div>
                                <div class="help-block with-errors col-sm-offset-4 col-sm-8"></div>
                            </div>    
                            <div class="form-group">
                                <div class="col-sm-offset-4 col-sm-8">
                                    <button type="submit" class="btn btn-primary" onclick="return this.attributes['class'].value.indexOf('disabled')==-1;">Enviar</button>
                                </div>
                            </div>
                            <%if(msg!=null){%>
                            <div class="form-group" style="<%=style%>">
                                <div class="col-sm-offset-4 col-sm-8">
                                    <%=msg%>
                                </div>                                
                            </div>     
                            <%}%>
                        </form>
                        <!--                      
                                            <form class="form-horizontal">
                                            <div class="form-group">
                                                <label for="inputPhoto" class="col-sm-2 control-label">My photo</label>
                                              <div class="col-sm-10">
                                                <input type="file" name="inputPhoto" id="inputPhoto" class="file">
                                                <p class="help-block">jpg with 160 by 160 pixels.</p>
                                              </div>
                                            </div>
                                            </form>
                        -->
                    </div><!-- /.tab-pane -->
                </div><!-- /.tab-content -->
            </div><!-- /.nav-tabs-custom -->
        </div><!-- /.col -->
    </div><!-- /.row -->
    <!--          
              <script type="text/javascript">
              $("#inputPhoto").fileinput({
                  maxFileCount:1,
                  uploadUrl: "/profile",
                  dropZoneEnabled: false,
                  showPreview: false,
              });
              </script>
    -->
        <script>
            $(function () {
                $('input').iCheck({
                    checkboxClass: 'icheckbox_square-blue',
                    radioClass: 'iradio_square-blue',
                    increaseArea: '20%' // optional
                });
            });
        </script>    
</section><!-- /.content -->
