/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.semanticwb.swbforms.utils;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.*;
import org.semanticwb.datamanager.DataMgr;
import org.semanticwb.datamanager.DataUtils;


/**
 *
 * @author javiersolis
 */
public class JarFile
{
    
    /** The log. */
    private static final Logger log = Logger.getLogger(JarFile.class.getName());

    /** The path. */
    private String path;

    /** The exists. */
    private boolean exists;
    
    /** The is dir. */
    private boolean isDir;
    
    /** The length. */
    private long length;
    
    /** The last modified. */
    private long lastModified;

    private long stamp=System.currentTimeMillis();

    private boolean replaced=false;
    
    /** The in. */
    //private InputStream in=null;
    
    /** The f. */
    private File f=null;
    
    /** The zip path. */
    //private String zipPath=null;

    /** The zip path. */
    private byte[] cache=null;
    
    //private ZipFile zf=null;
    private ZipEntry ze=null;

    /**
     * Instantiates a new jar file.
     * 
     * @param path the path
     */
    public JarFile(String path)
    {
        this.setPath(path);
    }    

    /**
     * Instantiates a new jar file.
     * 
     * @param f the f
     * @param zipPath the zip path
     */
    public JarFile(ZipEntry f, java.util.zip.ZipFile zf)
    {
        //this.zf=zf;
        this.ze=f;
        this.path = "/"+f.getName();
        exists=true;
        if(exists)
        {
            isDir=f.isDirectory();
            if(!isDir)
            {
                length=f.getSize();
                lastModified=f.getTime();
            }
            loadCache(zf);
        }else
        {

        }            
    }
    
    /**
     * Load cache.
     */
    private void loadCache()
    {
        loadCache(null);
    }    

    /**
     * Load cache.
     */
    private void loadCache(java.util.zip.ZipFile zf)
    {
        try
        {
            InputStream in=getInputStream(zf);
            ByteArrayOutputStream out=new ByteArrayOutputStream((int)length);
            if (null!=in) {
                DataUtils.copyStream(in, out);
                cache=out.toByteArray();
            }
        }catch(Exception e){log.log(Level.SEVERE, "Error loadCache:"+zf,e);}
    }
    
    /**
     * Gets the input stream.
     * 
     * @return the input stream
     */
    public InputStream getInputStream()
    {
        return getInputStream(null);
    }    

    /**
     * Gets the input stream.
     * 
     * @return the input stream
     */
    public InputStream getInputStream(java.util.zip.ZipFile zf)
    {
        try
        {
            if(ze!=null)
            {
                if(zf!=null)
                    return zf.getInputStream(ze);                
                else 
                    return getClass().getResourceAsStream(path);
                  //FileInputStream fis=new FileInputStream(zipPath);
                  //BufferedInputStream bis=new BufferedInputStream(fis);
                  //return new ZipInputStream(bis);                    
            }else
            {
                return new FileInputStream(f);
            }
        }catch(Exception e)
        {
            log.log(Level.SEVERE, "Error getInputStream:"+zf,e);
            return null;
        }
    }


    /**
     * Gets the cache.
     * 
     * @return the cache
     */
    public byte[] getCache()
    {
        return cache;
    }

    /**
     * Exists.
     * 
     * @return true, if successful
     */
    public boolean exists()
    {
        return exists;
    }

    /**
     * Checks if is directory.
     * 
     * @return true, if is directory
     */
    public boolean isDirectory()
    {
        return isDir;
    }    

    /**
     * Gets the path.
     * 
     * @return the path
     */
    public String getPath()
    {
        return path;
    }

    /**
     * Length.
     * 
     * @return the long
     */
    public long length()
    {
        return length;
    }

    /**
     * Last modified.
     * 
     * @return the long
     */
    public long lastModified()
    {
        return lastModified;
    }    

    /**
     * Sets the path.
     * 
     * @param p the new path
     */
    public void setPath(String p)
    {
        this.path = p;
        f=new File(DataMgr.getApplicationPath()+path);
        exists=f.exists();
        if(exists)
        {
            isDir=f.isDirectory();
            if(!isDir)
            {
                length=f.length();
                lastModified=f.lastModified();
            }
        }else
        {

        }
    }

    /**
     * Checks for cache.
     * 
     * @return true, if successful
     */
    public boolean hasCache()
    {
        return cache!=null;
    }

    public boolean isExpired()
    {
        return System.currentTimeMillis()>(stamp+60000L);
    }

    public void touch()
    {
        stamp=System.currentTimeMillis();
    }

    public boolean isReplaced()
    {
        return replaced;
    }

    public void setReplaced(boolean replaced)
    {
        this.replaced = replaced;
    }


}    