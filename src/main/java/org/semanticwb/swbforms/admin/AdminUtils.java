/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.semanticwb.swbforms.admin;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.SourceFile;
import java.util.Iterator;
import org.semanticwb.datamanager.*;

/**
 *
 * @author javiersolis
 */
public class AdminUtils {

    private static DataObject ds_cache = null;
    private static long ds_engineId = -1;

    private static boolean addAction(DataObject security, String action, String prop, DataList values) {
        DataObject act = security.addSubObject(action);
        DataList roles = values;
        if (roles != null && roles.size() > 0) {
            act.put(prop, roles);
            return true;
        }
        return false;
    }

    private static String getDataSourceScriptFromCache(SWBScriptEngine eng, boolean clientSide) {
        StringBuilder ret = new StringBuilder();
        Iterator it = ds_cache.values().iterator();
        while (it.hasNext()) {
            DataObject obj = (DataObject) it.next();
            boolean backend = obj.getBoolean("backend");
            boolean frontend = obj.getBoolean("frontend");
            DataList roles = obj.getDataList("roles");
            if ((clientSide && frontend) || (!clientSide && backend)) {
                if (roles == null || eng.hasUserAnyRole(roles) || (roles.size() == 1 && roles.contains("*")) || (eng.getUser() == null && !clientSide)) {
                    ret.append(obj.getString("text"));
                }
            }
        }
        return ret.toString();
    }

    public static String compile(SWBScriptEngine eng, String code) 
    {
        return compile(eng, code,null, false);
    }
    
    public static String compile(SWBScriptEngine eng, String code, String ext, boolean getErrors) 
    {
        if(!eng.getConfigData().getBoolean("compileDS",false))return code;
        com.google.javascript.jscomp.Compiler compiler = new com.google.javascript.jscomp.Compiler();

        CompilerOptions options = new CompilerOptions();
        // Advanced mode is used here, but additional options could be set, too.
        //CompilationLevel.ADVANCED_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
        CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);

        CompilerOptions.LanguageMode languageMode = CompilerOptions.LanguageMode.fromString("STABLE");
        options.setLanguageIn(languageMode);
        options.setLanguageOut(languageMode);

        //options.setCodingConvention(new ClosureCodingConvention());
        //options.setExtraAnnotationNames(flags.extraAnnotationName);
        // To get the complete set of externs, the logic in
        // CompilerRunner.getDefaultExterns() should be used here.
        SourceFile extern = SourceFile.fromCode("externs.js",
                "var console={log:function(txt){}};\n"
                +"function print(txt){};\n"
                +"function alert(txt){};\n"
                + "var eng = {\n"
                + "    dataStores:{},\n"
                + "    dataSources:{},\n"
                + "    dataSourceIndexes:{},\n"
                + "    fieldProcesors:{},\n"
                + "    validators:{},\n"
                + "    dataServices:{},\n"
                + "    dataProcessors:{},\n"
                + "    formProcessors:{},\n"
                + "    dataExtractors:{},\n"
                + "    fileSources:{},\n"
                + "    routes:{},\n"
                + "    _requires:[],\n"
                + "    isServerSide:true,\n"
                + "    getDataSource:function(){},\n"
                + "    require:function(source){}\n"
                + "};\n"
                +(ext!=null?ext:"")
        );

        // The dummy input name "input.js" is used here so that any warnings or
        // errors will cite line numbers in terms of input.js.
        SourceFile input = SourceFile.fromCode("input.js", code);

        // compile() returns a Result, but it is not needed here.
        //compiler.transpileAndDontCheck();
        //compiler.whitespaceOnlyPasses();
        compiler.compile(extern, input, options);

        // The compiler is responsible for generating the compiled code; it is not
        // accessible via the Result.
        if (compiler.hasErrors()) {
            StringBuilder txt = new StringBuilder();
            txt.append("Error:" + compiler.getErrorCount());
            txt.append("\n");
            Iterator<JSError> errs = compiler.getErrors().iterator();
            while (errs.hasNext()) {
                JSError err = errs.next();
                txt.append(err.lineNumber);
                txt.append(": ");
                txt.append("[" + err.getType().key + "] ");
                txt.append(err.description);
                txt.append("\n");
            }
            System.out.println(txt.toString() + "\n" + compiler.toSource());
            if(getErrors)return txt.toString() + "\n" + compiler.toSource();
        }

        return compiler.toSource();
    }

    public static String getDataSourceScriptFromDB(DataObject usr, boolean clientSide) {
        StringBuilder ret = new StringBuilder();
        SWBScriptEngine eng = DataMgr.getUserScriptEngine("/admin/ds/admin_base.js", usr, false);        
        if (eng.getId() != ds_engineId) {
            synchronized (eng) {
                if (eng.getId() != ds_engineId) {
                    ds_cache = new DataObject();
                    //System.out.println("Generaing AdminDS...");
                    try {
                        SWBDataSource ds = eng.getDataSource("ValueMap");
                        DataObjectIterator it = ds.find();
                        while (it.hasNext()) {
                            DataObject obj = it.next();

                            try {
                                String id = obj.getString("id");
                                DataList values = obj.getDataList("values");
                                DataObject query = new DataObject();
                                query.addSubObject("data").addParam("_id", values);
                                DataObject vm = new DataObject();
                                DataObjectIterator it2 = eng.getDataSource("ValueMapValues").find(query);
                                while (it2.hasNext()) {
                                    DataObject n = it2.next();
                                    vm.addParam(n.getString("id"), n.get("value"));
                                }
                                ret.append("var " + id + "=" + vm + ";\n");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        ds_cache.addSubObject("ValueMap").addParam("text", compile(eng,ret.toString())).addParam("backend", true).addParam("frontend", true);

                        ds = eng.getDataSource("Validator");
                        it = ds.find();
                        while (it.hasNext()) {
                            DataObject obj = it.next();

                            try {
                                String id = obj.getString("id");
                                String stype = obj.getString("type");
                                String errorMessage = obj.getString("errorMessage");

                                ret = new StringBuilder();
                                ret.append("eng.validators[\"" + id + "\"] = {");
                                ret.append("type: \"" + stype + "\"");
                                if (errorMessage != null) {
                                    ret.append(", errorMessage: \"" + errorMessage.replace("\"", "\\\"") + "\"");
                                }

                                DataObject query = new DataObject();
                                query.addSubObject("data").addParam("validator", obj.getId());
                                DataObjectIterator it3 = eng.getDataSource("ValidatorExt").find(query);
                                while (it3.hasNext()) {
                                    DataObject feobj = it3.next();
                                    String att = feobj.getString("att");
                                    ret.append(", " + att + ": ");
                                    Object value = feobj.get("value");
                                    String type = feobj.getString("type");
                                    if ("string".equals(type) || "date".equals(type)) {
                                        ret.append("\"" + value.toString().replace("\"", "\\\"") + "\"");
                                    } else {
                                        ret.append("" + value);
                                    }

                                }

                                ret.append("};" + "\n");
                                ds_cache.addSubObject("Validators_" + obj.getString("id")).addParam("text", compile(eng,ret.toString())).addParam("backend", true).addParam("frontend", true);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        ds = eng.getDataSource("DataSource");
                        it = ds.find();
                        while (it.hasNext()) {
                            try {
                                DataObject obj = it.next();

                                boolean backend = obj.getBoolean("backend");
                                boolean frontend = obj.getBoolean("frontend");

                                String modelid = obj.getString("modelid");
                                if (modelid == null) {
                                    modelid = eng.eval("_modelid").toString();
                                }
                                String dataStore = obj.getString("dataStore");
                                if (dataStore == null) {
                                    dataStore = eng.eval("_dataStore").toString();
                                }

                                ret = new StringBuilder();

                                ret.append("eng.dataSources[\"" + obj.getString("id") + "\"] = {" + "\n");
                                ret.append("    scls: \"" + obj.getString("scls") + "\"," + "\n");
                                ret.append("    modelid: \"" + modelid + "\"," + "\n");
                                ret.append("    dataStore: \"" + dataStore + "\"," + "\n");
                                ret.append("    dataSourceBase: \"" + obj.getId() + "\"," + "\n");
                                if (obj.getString("displayField") != null) {
                                    ret.append("    displayField: \"" + obj.getString("displayField") + "\"," + "\n");
                                }
                                if (obj.getString("valueField") != null) {
                                    ret.append("    valueField: \"" + obj.getString("valueField") + "\"," + "\n");
                                }
                                if (obj.getString("sortField") != null) {
                                    ret.append("    sortField: \"" + obj.getString("sortField") + "\"," + "\n");
                                }
                                ret.append("    fields: [" + "\n");

                                DataObject query = new DataObject();
                                query.addSubList("sortBy").add("order");
                                query.addSubObject("data").addParam("ds", obj.getId());
                                DataObjectIterator it2 = eng.getDataSource("DataSourceFields").find(query);
                                //System.out.println("size:"+it2.size()+":"+it2.total());
                                while (it2.hasNext()) {
                                    DataObject fobj = it2.next();
                                    //System.out.println("DataSourceFields:"+fobj);
                                    ret.append("        {name: \"" + fobj.getString("name") + "\"");
                                    if (fobj.getString("title") != null) {
                                        ret.append(", title: \"" + fobj.getString("title") + "\"");
                                    }
                                    if (fobj.getString("type") != null) {
                                        ret.append(", type: \"" + fobj.getString("type") + "\"");
                                    }
                                    if (fobj.getString("required") != null) {
                                        ret.append(", required: " + fobj.getString("required") + "");
                                    }

                                    query = new DataObject();
                                    query.addSubObject("data").addParam("dsfield", fobj.getId());
                                    DataObjectIterator it3 = eng.getDataSource("DataSourceFieldsExt").find(query);
                                    while (it3.hasNext()) {
                                        DataObject feobj = it3.next();
                                        String att = feobj.getString("att");
                                        ret.append(", " + att + ": ");
                                        Object value = feobj.get("value");
                                        String type = feobj.getString("type");
                                        if (att.equals("validators")) {
                                            if (value instanceof DataList) {
                                                DataList list = (DataList) value;
                                                ret.append("[");
                                                Iterator<String> it4 = list.iterator();
                                                while (it4.hasNext()) {
                                                    String val = it4.next();
                                                    //ret.append("{stype: \""+val+"\"}");
                                                    ret.append("eng.validators[\"" + val + "\"]");
                                                    if (it4.hasNext()) {
                                                        ret.append(",");
                                                    }
                                                }
                                                ret.append("]");
                                            } else {
                                                ret.append("[");
                                                if (value != null) {
                                                    String vals[] = value.toString().split(",");
                                                    for (int i = 0; i < vals.length; i++) {
                                                        //ret.append("{stype: \""+vals[i]+"\"}");
                                                        ret.append("eng.validators[\"" + vals[i] + "\"]");
                                                        if (i + 1 < vals.length) {
                                                            ret.append(",");
                                                        }
                                                    }
                                                }
                                                ret.append("]");
                                            }
                                        } else if ("string".equals(type) || "date".equals(type))// || value instanceof String) 
                                        {
                                            ret.append("\"" + value.toString().replace("\"", "\\\"") + "\"");
                                        } else {
                                            ret.append("" + value);
                                        }

                                    }
                                    ret.append("}," + "\n");
                                }

                                ret.append("    ]," + "\n");

                                if (!clientSide) {
                                    boolean hasSecurity = false;
                                    DataObject security = new DataObject();

                                    if (addAction(security, "fetch", "roles", obj.getDataList("roles_fetch"))) {
                                        hasSecurity = true;
                                    }
                                    if (addAction(security, "add", "roles", obj.getDataList("roles_add"))) {
                                        hasSecurity = true;
                                    }
                                    if (addAction(security, "update", "roles", obj.getDataList("roles_update"))) {
                                        hasSecurity = true;
                                    }
                                    if (addAction(security, "remove", "roles", obj.getDataList("roles_remove"))) {
                                        hasSecurity = true;
                                    }

                                    if (hasSecurity) {
                                        ret.append("    security:");
                                        ret.append(security.toString(true));
                                        ret.append("," + "\n");
                                    }
                                }
                                ret.append("};" + "\n");

                                ds_cache.addSubObject("DataSource_" + obj.getString("id")).addParam("text", compile(eng,ret.toString())).addParam("backend", backend).addParam("frontend", frontend).addParam("roles", obj.getDataList("roles_fetch"));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        {
                            ds = eng.getDataSource("DataSourceIndex");
                            it = ds.find();
                            while (it.hasNext()) {
                                DataObject obj = it.next();
                                try {
                                    String dataSource = obj.getString("ds");
                                    String name = obj.getString("name");
                                    String dsn = dataSource.substring(dataSource.lastIndexOf(":") + 1);

                                    ret = new StringBuilder();
                                    ret.append("eng.dataSourceIndexes[\"" + name + "\"] = {" + "\n");
                                    ret.append("    dataSource: \"" + dsn + "\"," + "\n");
                                    ret.append("    scriptEngine: \"/admin/ds/datasources.js\"," + "\n");

                                    ret.append("    index: {");
                                    DataObject query = new DataObject();
                                    query.addSubObject("data").addParam("dsindex", obj.getId());
                                    DataObjectIterator it2 = eng.getDataSource("DataSourceIndexFields").find(query);
                                    //System.out.println("size:"+it2.size()+":"+it2.total());
                                    while (it2.hasNext()) {
                                        DataObject fobj = it2.next();
                                        ret.append(fobj.getString("prop").substring(dsn.length() + 1));
                                        ret.append(":");
                                        ret.append(fobj.getString("type"));
                                        if (it2.hasNext()) {
                                            ret.append(", ");
                                        }
                                    }
                                    ret.append("}" + "\n");

                                    ret.append("};" + "\n");
                                    ds_cache.addSubObject("DataSourceIndex_" + obj.getString("name")).addParam("text", compile(eng,ret.toString())).addParam("backend", true).addParam("frontend", false);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            StringBuilder ext=new StringBuilder();
                            ds = eng.getDataSource("GlobalScript");
                            DataObject query = new DataObject();
                            query.addSubList("sortBy").add("order");
                            it = ds.find(query);
                            while (it.hasNext()) {
                                DataObject obj = it.next();
                                try {
                                    if (!obj.getBoolean("active", false)) {
                                        continue;
                                    }

                                    String id = obj.getString("id");
                                    String script = obj.getString("script");
                                    int order = obj.getInt("order");

                                    ret = new StringBuilder();
                                    ret.append("\n");
                                    ret.append(script);
                                    ret.append("\n\n");
                                    ds_cache.addSubObject("GlobalScript_" + obj.getString("id")).addParam("text", compile(eng,ret.toString())).addParam("backend", true).addParam("frontend", false);
                                    ext.append(ret.toString());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            ds = eng.getDataSource("DataProcessor");
                            it = ds.find();
                            while (it.hasNext()) {
                                DataObject obj = it.next();
                                try {
                                    if (!obj.getBoolean("active", false)) {
                                        continue;
                                    }

                                    String id = obj.getString("id");
                                    DataList dataSources = obj.getDataList("dataSources", new DataList());
                                    DataList actions = obj.getDataList("actions", new DataList());
                                    String request = obj.getString("request");
                                    String response = obj.getString("response");
                                    int order = obj.getInt("order");

                                    ret = new StringBuilder();
                                    ret.append("eng.dataProcessors[\"" + id + "\"] = {" + "\n");
                                    ret.append("    dataSources: " + dataSources + "," + "\n");
                                    ret.append("    actions: " + actions + "," + "\n");
                                    ret.append("    order: " + order + "," + "\n");
                                    if (request != null && request.trim().length() > 0) {
                                        ret.append("    request: " + request.replace("\n", "\n    ") + "," + "\n");
                                    }
                                    if (response != null && response.trim().length() > 0) {
                                        ret.append("    response: " + response.replace("\n", "\n    ") + "," + "\n");
                                    }
                                    ret.append("};" + "\n");
                                    ds_cache.addSubObject("DataProcessor_" + obj.getString("id")).addParam("text", compile(eng,ret.toString(),ext.toString(),false)).addParam("backend", true).addParam("frontend", false);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            
                            ds = eng.getDataSource("FormProcessor");
                            it = ds.find();
                            while (it.hasNext()) {
                                DataObject obj = it.next();
                                try {
                                    if (!obj.getBoolean("active", false)) {
                                        continue;
                                    }

                                    String id = obj.getString("id");
                                    DataList dataSources = obj.getDataList("dataSources", new DataList());
                                    DataList actions = obj.getDataList("actions", new DataList());
                                    String request = obj.getString("request");
                                    int order = obj.getInt("order");

                                    ret = new StringBuilder();
                                    ret.append("eng.formProcessors[\"" + id + "\"] = {" + "\n");
                                    ret.append("    dataSources: " + dataSources + "," + "\n");
                                    ret.append("    actions: " + actions + "," + "\n");
                                    ret.append("    order: " + order + "," + "\n");
                                    if (request != null && request.trim().length() > 0) {
                                        ret.append("    request: " + request.replace("\n", "\n    ") + "," + "\n");
                                    }
                                    ret.append("};" + "\n");
                                    ds_cache.addSubObject("FormProcessor_" + obj.getString("id")).addParam("text", compile(eng,ret.toString(),ext.toString(),false)).addParam("backend", true).addParam("frontend", false);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }                            

                            ds = eng.getDataSource("DataService");
                            it = ds.find();
                            while (it.hasNext()) {
                                DataObject obj = it.next();
                                try {
                                    if (!obj.getBoolean("active", false)) {
                                        continue;
                                    }

                                    String id = obj.getString("id");
                                    DataList dataSources = obj.getDataList("dataSources", new DataList());
                                    DataList actions = obj.getDataList("actions", new DataList());
                                    String service = obj.getString("service");
                                    int order = obj.getInt("order");

                                    ret = new StringBuilder();
                                    ret.append("eng.dataServices[\"" + id + "\"] = {" + "\n");
                                    ret.append("    dataSources: " + dataSources + "," + "\n");
                                    ret.append("    actions: " + actions + "," + "\n");
                                    ret.append("    order: " + order + "," + "\n");
                                    if (service != null && service.trim().length() > 0) {
                                        ret.append("    service: " + service.replace("\n", "\n    ") + "," + "\n");
                                    }
                                    ret.append("};" + "\n");
                                    ds_cache.addSubObject("DataService_" + obj.getString("id")).addParam("text", compile(eng,ret.toString(),ext.toString(),false)).addParam("backend", true).addParam("frontend", false);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            ds = eng.getDataSource("DataExtractor");
                            it = ds.find();
                            while (it.hasNext()) {
                                DataObject obj = it.next();
                                if (!obj.getBoolean("active", false)) {
                                    continue;
                                }

                                String id = obj.getString("id");
                                String scriptEngine = obj.getString("scriptEngine", "/admin/ds/datasources.js");
                                String dataSource = obj.getString("dataSource");
                                String start = obj.getString("start");
                                String extract = obj.getString("extract");
                                String stop = obj.getString("stop");
                                int first_time = obj.getInt("first_time");
                                String first_unit = obj.getString("first_unit");
                                int time = obj.getInt("time");
                                String unit = obj.getString("unit");

                                ret = new StringBuilder();
                                ret.append("eng.dataExtractors[\"" + id + "\"] = {" + "\n");
                                ret.append("    scriptEngine:\"" + scriptEngine + "\"," + "\n");
                                ret.append("    dataSource: \"" + dataSource + "\"," + "\n");
                                ret.append("    extractor:{\n");
                                if (start != null && start.trim().length() > 0) {
                                    ret.append("        start: " + start.replace("\n", "\n        ") + "," + "\n");
                                }
                                if (extract != null && extract.trim().length() > 0) {
                                    ret.append("        extract: " + extract.replace("\n", "\n        ") + "," + "\n");
                                }
                                if (stop != null && stop.trim().length() > 0) {
                                    ret.append("        stop: " + stop.replace("\n", "\n        ") + "," + "\n");
                                }
                                ret.append("    },\n");
                                ret.append("    timer:{\n");
                                ret.append("        first_time: " + first_time + "," + "\n");
                                ret.append("        first_unit: \"" + first_unit + "\"," + "\n");
                                ret.append("        time: " + time + "," + "\n");
                                ret.append("        unit: \"" + unit + "\"" + "\n");
                                ret.append("    }\n");
                                ret.append("};" + "\n");
                                ds_cache.addSubObject("DataExtractor_" + obj.getString("id")).addParam("text", compile(eng,ret.toString(),ext.toString(),false)).addParam("backend", true).addParam("frontend", false);
                            }
                        }
                        ds_engineId = eng.getId();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return getDataSourceScriptFromCache(eng, clientSide);
    }

}
