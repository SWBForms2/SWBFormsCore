var _modelid = java.lang.System.getProperty("_modelid","SWBForms");
var _dataStore = java.lang.System.getProperty("_dataStore","mongodb");

//******* Validations ************
eng.validators["unique"] = {type: "isUnique", errorMessage: "El valor del campo debe de ser único"};
eng.validators["id"] = {type: "regexp", expression: "^([a-zA-Z0-9_+])+$", errorMessage: "Identificador no valido"};
eng.validators["email"] = {type: "regexp", expression: "^([a-zA-Z0-9_.\\-+])+@(([a-zA-Z0-9\\-])+\\.)+[a-zA-Z0-9]{2,4}$", errorMessage: "Correo electrónico invalido"};

eng.config = {
    baseDatasource: "/WEB-INF/global.js",
    appName: "SWBForms",
    dsCache: true,
    mail: {
        from: "xxx@gmail.com",
        fromName: "Name",
        host: "smtp.gmail.com",
        user: "xxx@gmail.com",
        passwd: "password",
        port:465,
        transport: "smtps", //smtp, smtps        
        ssltrust: "*",
        starttls: "true"           
    },     
/*    
    startup: {
        cloudino: {
            class: "io.cloudino.Loader",
            dataSourcePath: "/admin/js/cloudino.js",
            port: 9494,
        }
    }
*/    
};

//******* DataStores ***************
eng.dataStores["mongodb"] = {
    host: "localhost",
    port: 27017,
    //clientURI="mongodb://user1:pwd1@host1/?authSource=db1&ssl=true",                              //Connection by clientURI
    //clientURI: "mongodb://XXXXXXXX.documents.azure.com:10255/?ssl=true&replicaSet=globaldb",      //CosmosDB  Microsoft
    //envHost="host";                                                                               //host defined by enviroment variable
    //envPort="port";                                                                               //port defined by enviroment variable
    //envClientURI="clientURI";                                                                     //clientURI defined by enviroment variable
    class: "org.semanticwb.datamanager.datastore.DataStoreMongo",
};

//******* DataStores ***************
eng.dataStores["embedmongodb"] = {
    host: "localhost",
    port: 27017,
    dbPath: "{appPath}/../embedmongo",
    dbDataPath: "{appPath}/../embedmongo/data",  
    class: "org.semanticwb.datamanager.datastore.DataStoreEmbedMongo"
};

/*
eng.dataStores["ts_leveldb"]={
    path:"/data/leveldb",
    class: "org.semanticwb.datamanager.datastore.SemDataStoreLevelDB",
};
*/

//******* DataSources ************
var roles = {prog: "Programmer", su: "Super User", admin: "Admin", user: "User"};

/******* Routes ************/
eng.routes["global"] = {
    loginFallback: "login",
    routeList: [
        {routePath: "login", forwardTo: "/admin/login.jsp", isRestricted: "false", zindex: 1},
        {routePath: "register", forwardTo: "/admin/register.jsp", isRestricted: "false"},
        {routePath: "passwordRecovery", forwardTo: "/admin/passwordRecovery.jsp", isRestricted: "false"},
        {routePath: "validator", forwardTo: "/admin/validator.jsp", isRestricted: "false", zindex: 1},
        {routePath: "", forwardTo: "/index.jsp", isRestricted: "false"},
        {routePath: "admin/*", jspMapTo: "/admin/jsp/", isRestricted: "true"},
        //{routePath: "api/*", jspMapTo: "/api/", isRestricted: "false"},
        {routePath: "ds", forwardTo: "/platform/jsp/datasource.jsp", isRestricted: "true"},
        {routePath: "ex", forwardTo: "/platform/jsp/export.jsp", isRestricted: "true"},
    ],
};

eng.require("/admin/ds/app_config.js",false);

eng.dataSources["User"]={
    scls: "User",
    modelid: _modelid,
    dataStore: _dataStore,  
    displayField: "fullname",
    fields:[
        {name:"fullname",title:"Nombre",type:"string"},
        {name:"password",title:"Contraseña",type:"password"},
        {name:"email",title:"Correo electrónico",type:"string", validators:[{stype:"unique"}]},
        {name:"roles",title:"Roles",stype:"select", valueMap:roles, multiple:true},
        {name: "created", title: "Fecha de Registro", type: "date", editorType: "StaticTextItem", canEdit: false},
        {name: "creator", title: "Usuario Creador", type: "string", editorType: "StaticTextItem", canEdit: false},
        {name: "updated", title: "Ultima Actualización", type: "date", editorType: "StaticTextItem", canEdit: false},
        {name: "updater", title: "Usuario Ultima Actualización", type: "string", editorType: "StaticTextItem", canEdit: false},
    ],
    //security:{"fetch":{"roles":["sys","prog", "su", "admin"]}, "add":{"roles":["sys","prog", "su", "admin"]}, "update":{"roles":["prog", "su", "admin"]}, "remove":{"roles":["prog", "su"]}},    
};

eng.dataSources["Permission"]={
    scls: "Permission",
    modelid: _modelid,
    dataStore: _dataStore,  
    displayField: "name",
    fields:[
        {name:"name",title:"Nombre",type:"string"},
        {name:"roles",title:"Roles",stype:"select", valueMap:roles,multiple:true},
        {name: "created", title: "Fecha de Registro", type: "date", editorType: "StaticTextItem", canEdit: false},
        {name: "creator", title: "Usuario Creador", type: "string", editorType: "StaticTextItem", canEdit: false},
        {name: "updated", title: "Ultima Actualización", type: "date", editorType: "StaticTextItem", canEdit: false},
        {name: "updater", title: "Usuario Ultima Actualización", type: "string", editorType: "StaticTextItem", canEdit: false},
    ],
    //security:{"fetch":{"roles":["prog"]}, "add":{"roles":["prog"]}, "update":{"roles":["prog"]}, "remove":{"roles":["prog"]}},
};

/******* DataProcessors ************/

eng.dataProcessors["UserProcessor"] = {
    dataSources: ["User"],
    actions: ["fetch", "add", "update"],
    request: function (request, dataSource, action, trxParams)
    {
        if (request.data && request.data.password)
        {
            request.data.password = this.utils.encodeSHA(request.data.password);
        }
        return request;
    }
};

eng.dataSources["DSCounter"] = {
    scls: "DSCounter",
    modelid: _modelid,
    dataStore: _dataStore,
    displayField: "namr",
    fields: [
        {name: "scls", title: "Collección", type: "string"},
        {name: "field", title: "Propiedad", type: "string"},
        {name: "count", title: "Contador", type: "long"},
    ],
    //security:{"fetch":{"roles":["sys","prog"]}, "add":{"roles":["sys","prog"]}, "update":{"roles":["sys","prog"]}, "remove":{"roles":["prog"]}},  
};

eng.dataSources["Log"] = {
    scls: "Log",
    modelid: _modelid,
    dataStore: _dataStore,
    displayField: "user",
    fields: [
        //{name: "source", title: "Source", type: "string"},
        {name: "user", title: "Usuario", type: "string"},
        {name: "dataSource", title: "DataSource", type: "string"},
        {name: "action", title: "Acción", type: "string"},
        {name: "data", title: "Datos", type: "boolean"}, //Se define como boolean para que internamente se interprete como objeto
        {name: "timestamp", title: "TimeStamp", type: "long"},
    ],
    //security:{"fetch":{"roles":["prog","su"]}, "add":{"roles":["sys"]}}, 
};


//******* DataProcessors ************
eng.dataProcessors["DefPropertiesProcessor"] = {
    dataSources: ["*"],
    actions: ["add", "update"],
    order: -999,
    request: function (request, dataSource, action, trxParams)
    {
        if (dataSource !== "Log" && dataSource !== "DSCounter")
        {
            var ds = this.getDataSource(dataSource);
            var scls=ds.getClassName();
            if (action === "add")
            {
                //if field type == "autogen"                
                var fs = ds.findScriptFields("stype", "autogen");
                for (var i = 0; i < fs.size(); i++)
                {
                    //print("fs:"+fs[i].getString("name"));
                    if (!request.data[fs[i].getString("name")] || request.data[fs[i].getString("name")].length == 0)
                    {
                        var DataUtils = Java.type('org.semanticwb.datamanager.DataUtils');
                        var id = DataUtils.createId();
                        request.data[fs[i].getString("name")] = id;
                    }
                }
                
                //if field type == "sequence"
                fs = ds.findScriptFields("stype", "sequence");
                for (var i = 0; i < fs.size(); i++)
                {
                    if (!request.data[fs[i].getString("name")] || request.data[fs[i].getString("name")].length == 0)
                    {
                        var id = 0;
                        var r = this.getDataSource("DSCounter").find({"data": {"scls": scls, "field": fs[i].getString("name")}});
                        if (r.hasNext())
                        {
                            var o = r.next();
                            o.count++;
                            id = o.count;
                            this.getDataSource("DSCounter").updateObj(o);
                        } else
                        {
                            id++;
                            this.getDataSource("DSCounter").addObj({"scls": scls, "field": fs[i].getString("name"), "count": id});
                        }                        
                        request.data[fs[i].getString("name")] = id;
                    }
                }          
                
                //if field type == "sequence"
                fs = ds.findScriptFields("stype", "id");
                if(fs.size()==1  && request.data["_id"]==null)
                {
                    request.data["_id"] = ds.getBaseUri() + request.data[fs[0].getString("name")];
                }                
                
                //if properti name == "id"
                if(request.data["id"]!=null && request.data["_id"]==null)
                {
                    request.data["_id"] = ds.getBaseUri() + request.data["id"];
                }

                if (ds.findScriptFields("name","created") != null)
                {
                    request.data.created = new Date().toISOString();
                    if (this.user)
                    {
                        request.data.creator = this.user.email;
                    }
                }
            } else if (action === "update")
            {
                if (ds.findScriptFields("name","updated") != null)
                {
                    request.data.updated = new Date().toISOString();
                    if (this.user)
                    {
                        request.data.updater = this.user.email;
                    }
                }
            }
            {
                var fs = ds.findScriptFields("type", "time");
                for (var i = 0; i < fs.size(); i++)
                {
                    var t=request.data[fs[i].getString("name")];
                    if(t)
                    {
                        var j=t.indexOf(".");
                        if(j>-1)request.data[fs[i].getString("name")]=t.substring(0,j);
                    }
                }
            }   
            {
                var fs = ds.findScriptFields("validators", "type", "isUnique");
                for (var i = 0; i < fs.size(); i++)
                {
                    var prop=fs[i].getString("name");
                    var val=request.data[prop];
                    if(val)
                    {
                        var id=request.data._id;
                        var obj=this.getDataSource(dataSource).fetchObjByProp(prop,val);
                        if(obj!=null && !obj._id.equals(id))
                        {
                            throw "Error "+fs[i].getString("title")+": "+org.semanticwb.datamanager.DataUtils.getArrayNode(fs[i].get("validators"), "type", "isUnique").getString("errorMessage");
                        }
                    }
                }                
            }
            if (request.data._swbf_processAction != null)
            {
                var err=this.getProcessMgr().processAction(this,request.data,trxParams);
                if(err!=null)throw err;
            }
        }
        return request;
    },
};

//implementación del lado del eng.js (cliente)
eng.getProcessStates=function(){return {}};
