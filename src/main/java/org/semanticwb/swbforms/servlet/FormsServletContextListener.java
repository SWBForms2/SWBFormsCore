/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.semanticwb.swbforms.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.semanticwb.datamanager.*;
import org.semanticwb.datamanager.script.ScriptObject;

@WebListener
public class FormsServletContextListener implements ServletContextListener {

    static Logger logger = Logger.getLogger(FormsServletContextListener.class.getName());
    
    private String parseId(String id, SWBScriptEngine eng)
    {
        if(id.startsWith("_suri:"))
        {
            String modelid=null;
            String clsname=null;
            String nid=null;

            int i1 = id.indexOf(":");
            if (i1 > -1) {
                int i2 = id.indexOf(":", i1 + 1);
                if (i2 > -1) {
                    modelid=id.substring(i1 + 1, i2);
                    int i3 = id.indexOf(":", i2 + 1);
                    if (i3 > -1) 
                    {
                        clsname=id.substring(i2 + 1, i3);
                        nid=id.substring(i3 + 1);

                        id=eng.getDataSource(clsname).getBaseUri()+nid;
                    }
                }
            }                              
        }   
        return id;
    }    

    @Override
    public void contextInitialized(ServletContextEvent sce) {        
        logger.info("Starting SWBForms");
        DataMgr.createInstance(sce.getServletContext());
        logger.info("SWBForms DataMgr Started");
        
        File fl=new File(DataMgr.getApplicationPath()+"/WEB-INF/classes/loggin.properties");
        if(fl.exists())
        {
            try
            {
                FileInputStream props=new FileInputStream(fl);
                LogManager.getLogManager().readConfiguration(props);
                logger=Logger.getLogger(FormsServletContextListener.class.getName());
                logger.info("Configuring logging.properties");
            }catch(Exception e)
            {
                logger.log(Level.WARNING,"Error processing logging.properties",e);
            }
        }else
        {
            logger.info("Not logging.properties found");            
        }

        SWBScriptEngine engine = DataMgr.getUserScriptEngine("/WEB-INF/global.js", (DataObject)null, false);
        //logger.info("SWBForms SWBScriptEngine Started");
        
        
        //Config Startup
        ScriptObject config = engine.getScriptObject().get("config");
        if (config != null) {
            String base = config.getString("baseDatasource");
            if (base != null) {
                DataMgr.getBaseInstance().setBaseDatasourse(base);
            }            
            
            //Install base data
            if(!engine.getDataSource("DSCounter").getDataStore().existModel(engine.getDataSource("DSCounter").getModelId()))
            {
                logger.log(Level.INFO,"Initializing Administration...");
                logger.log(Level.INFO,"Importing /admin/ds/admin.js");
                //engine.getDataSource("DSCounter")

                SWBScriptEngine eng = DataMgr.getUserScriptEngine("/admin/ds/admin.js", (DataObject)null, false);            
                try
                {
                    FileInputStream fin = new FileInputStream(DataMgr.getApplicationPath() + "/WEB-INF/admindb.json");
                    DataObject data = (DataObject) DataObject.parseJSON(DataUtils.readInputStream(fin, "utf8"));

                    eng.setDisabledDataTransforms(true);
                    Iterator<String> keys = data.keySet().iterator();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        //out.println(key);
                        DataList<DataObject> list=data.getDataList(key);
                        for(DataObject ele:list)
                        {
                            //out.println("  "+ele);
                            SWBDataSource ds=eng.getDataSource(key);

                            //convertir Model_Ids
                            Iterator<String> keys2=ele.keySet().iterator();
                            while (keys2.hasNext()) {
                                String key2 = keys2.next();
                                if(key2.equals("_id"))continue;
                                Object obj=ele.get(key2);
                                if(obj instanceof String)
                                {
                                    String id=(String)obj;
                                    String id2=parseId(id,eng);
                                    if(!id.equals(id2))ele.put(key2,id2);
                                }else if(obj instanceof DataList)
                                {
                                    //out.println(key2+"->"+obj);

                                    DataList dl=(DataList)obj;
                                    for(int x=0;x<dl.size();x++)
                                    {
                                        Object obj2=dl.get(x);
                                        if(obj2 instanceof String)
                                        {
                                            String id=(String)obj2;
                                            String id2=parseId(id,eng);
                                            //out.println("    "+id+"->"+id2);
                                            if(!id.equals(id2))dl.set(x,id2);
                                        }
                                    }

                                }else
                                {
                                    //out.println(obj);
                                }
                            }

                            String _id=ds.getBaseUri()+ele.getNumId();
                            ele.addParam("_id", _id);
                            //out.println("  "+ele);
                            if(ds.fetchObjById(_id)!=null)
                            {
                                ds.updateObj(ele);
                            }else
                            {
                                ds.addObj(ele);
                            }
                        }
                    }    
                }catch(Exception e)
                {
                    logger.log(Level.SEVERE,"Error importing administration data ",e);
                }finally
                {
                    eng.setDisabledDataTransforms(false);
                }
                eng.reloadAllScriptEngines();            

            }                        
            
            //Startup
            ScriptObject startup = config.get("startup");
            if(startup!=null)
            {
                DataObject st=startup.toDataObject();
                Iterator<String> it=st.keySet().iterator();
                while (it.hasNext()) {
                    String objname = it.next();
                    DataObject data=st.getDataObject(objname);
                    logger.log(Level.INFO,"Startup " + objname + "...");
                    String clsname = data.getString("class");
                    try
                    {
                        Class cls = Class.forName(clsname);      
                        Constructor c=cls.getConstructor(DataObject.class);
                        c.newInstance(data);
                    } catch (Exception e)
                    {
                        logger.log(Level.WARNING,"Startup load initialization error "+ objname ,e);
                    }                    
                }
            }            
        }               
        
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.log(Level.INFO,"Web Application Stoped");
        DataMgr.stopInstance();
    }
}
