package org.semanticwb.swbforms.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.semanticwb.swbforms.utils.JarFile;
import org.semanticwb.datamanager.DataMgr;
import org.semanticwb.datamanager.DataObject;
import org.semanticwb.datamanager.DataUtils;
import org.semanticwb.datamanager.RouteData;
import org.semanticwb.datamanager.RoutesMgr;
import org.semanticwb.datamanager.SWBDataSource;
import org.semanticwb.datamanager.monitor.SWBMonitorMgr;
import org.semanticwb.datamanager.script.ScriptObject;
import org.semanticwb.swbforms.servlet.router.RouteHandler;

/**
 *
 * @author serch
 */
@WebFilter(urlPatterns = {"/*"})
public class FormsFilter implements Filter {
    
    
    /** The date formats. */
    protected static SimpleDateFormat dateFormats[];
    
     /**
     * Contains the names and content of all files within admin.zip
     * <p>
     * Contiene los nombres y el contenido de todos los archivos dentro de los
     * archivos admin.zip.</p>
     */
    private static HashMap admFiles = null;
    
    /** Support gzip encoding */    
    boolean agzip=true;    

    private static final Logger logger = Logger.getLogger(FormsFilter.class.getName());
    //private static final Router router = Router.getRouter();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("Starting SWBForms...");
        loadAdminFiles();
    }
    
    /**
     * Load admin files.
     */
    public void loadAdminFiles() {
        logger.info("Loading Static Files...");
        admFiles = new HashMap();
        try {
            logger.info("Loading Static Files from: /WEB-INF/SWBFormsStatic-1.0-SNAPSHOT.jar");
            String zipPath = DataMgr.getApplicationPath() + "/WEB-INF/SWBFormsStatic-1.0-SNAPSHOT.jar";
            ZipFile zf = new ZipFile(zipPath);
            Enumeration e = zf.entries();
            while (e.hasMoreElements()) {
                ZipEntry ze = (ZipEntry) e.nextElement();
                logger.fine("/" + ze.getName() + ", " + ze.getSize() + ", " + ze.getTime());
                admFiles.put("/" + ze.getName(), new JarFile(ze, zf));
            }
            zf.close();
            //log.event("-->Admin Files in Memory:\t" + admFiles.size());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading static files", e);
        }
    }    

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException 
    {
        Object obj = ((HttpServletRequest) request).getSession().getAttribute("_USER_");
//        System.out.println("obj:"+obj);
        DataObject user = null;
        if ((null != obj) && (obj instanceof DataObject)) {
            user = (DataObject) obj;
        }
        HttpServletRequest hreq = ((HttpServletRequest) request);
//        System.out.println("*************************************");
//        System.out.println("getContextPath:"+hreq.getContextPath());
//        System.out.println("getPathInfo:"+hreq.getPathInfo());
//        System.out.println("getPathTranslated:"+hreq.getPathTranslated());
//        System.out.println("getRequestURI:"+hreq.getRequestURI());
//        System.out.println("getServletPath:"+hreq.getServletPath());
//        System.out.println("getRequestURL:"+hreq.getRequestURL());
        String path = hreq.getRequestURI().substring(hreq.getContextPath().length());
//        System.out.println("path->:"+path);

        RouteData data = RoutesMgr.getRouteData(getRouterPath(path));
        RouteHandler handler = null;
        if (data != null) {
            if (!(data.isSecure() && user == null)) {
                handler = (RouteHandler) data.getHandler();
                if (handler == null) {
                    synchronized (data) {
                        if (handler == null) {
                            handler = getHandler(data);
                        }
                    }
                }
            } else {
                RouteData login = RoutesMgr.getRouteData(RoutesMgr.getLoginRoute());
                if (login != null) {
                    handler = (RouteHandler) login.getHandler();
                    if (handler == null) {
                        synchronized (login) {
                            if (handler == null) {
                                handler = getHandler(login);
                            }
                        }
                    }
                }
            }
        }
        
        if(processStaticFile(path, (HttpServletRequest)request, (HttpServletResponse)response))return;
        
        SWBMonitorMgr.startMonitor("/wp"+path);
        try
        {        
            if (null == handler) {
                chain.doFilter(request, response);
            } else {
                handler.handle(hreq, (HttpServletResponse) response);
            }
        }finally
        {
            SWBMonitorMgr.endMonitor();
        }        
    }

    @Override
    public void destroy() {

    }

    private String getRouterPath(final String path) {
        final String routeb = path.substring(1);
        String route = null;
        if (routeb.contains("/")) {
            route = routeb.substring(0, routeb.indexOf("/"));
        } else {
            route = routeb;
        }
        if (RoutesMgr.getRouteData(routeb) != null) {
            return routeb;
        }
        final String routeJsp = routeb.substring(0, routeb.lastIndexOf("/") + 1) + "*";

        if (RoutesMgr.getRouteData(routeJsp) != null) {
            return routeJsp;
        }
        //System.out.println("getRouterPath:" + path + "->" + route);
        return route;
    }

    private RouteHandler getHandler(RouteData data) {
        ScriptObject path = data.getScriptObject();
        //System.out.println("getHandler:" + path);
        try {
//            if ("true".equalsIgnoreCase(path.getString("isRestricted"))) {
//                securedRoutes.add(path.getString("routePath"));
//            }
            if (null != path.getString("forwardTo")) {
                final String jspRoute = path.getString("forwardTo");
                RouteHandler rh = (HttpServletRequest request, HttpServletResponse response) -> {
                    request.setAttribute("servletPath", request.getServletPath());
                    request.getServletContext().getRequestDispatcher(jspRoute).forward(request, response);
                };
                //routes.put(path.getString("routePath"), rh);
                data.setHandler(rh);
                return rh;
            }
            if (null != path.getString("jspMapTo")) {
                final String jspRoute = path.getString("jspMapTo");
                RouteHandler rh = new RouteHandler() {
                    private final String mapTo = jspRoute;

                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
                        String name = request.getRequestURI();
                        name = mapTo + name.substring(name.lastIndexOf("/") + 1) + ".jsp";
                        logger.fine("name: " + name);
                        logger.fine("realName: " + request.getServletContext().getRealPath(name));
                        if (Files.exists(Paths.get(request.getServletContext().getRealPath(name)))) {
                            request.setAttribute("servletPath", request.getServletPath());
                            request.getServletContext().getRequestDispatcher(name).forward(request, response);
                        } else {
                            name = request.getRequestURI().substring(request.getContextPath().length()).substring(1);
                            name = name.substring(0, name.indexOf("/"));

                            RouteData rd = RoutesMgr.getRouteData(name);

                            if (rd != null && rd.getHandler()!=null) {
                                logger.fine("encontré handler de " + name);
                                ((RouteHandler) rd.getHandler()).handle(request, response);
                            } else {
                                name = request.getRequestURI();
                                name = mapTo + name.substring(name.lastIndexOf("/") + 1) + "index.jsp";
                                logger.fine("buscando si hay " + name);
                                if (Files.exists(Paths.get(request.getServletContext().getRealPath(name)))) {
                                    request.setAttribute("servletPath", request.getServletPath());
                                    request.getServletContext().getRequestDispatcher(name).forward(request, response);
                                } else {
                                    response.sendError(404, request.getRequestURI() + " not found!");
                                }
                            }
                        }
                    }
                };
                //routes.put(path.getString("routePath"), rh);
                data.setHandler(rh);
                return rh;
            }
            if (null == path.getString("routeHandler")) {
                return null;
            }
            RouteHandler rh = (RouteHandler) Class.forName(path.getString("routeHandler")).newInstance();
            //routes.put(path.getString("routePath"), rh);
            data.setHandler(rh);
            return rh;
        } catch (Exception cnf) {
            logger.log(Level.SEVERE, "****** Can''t load class: {0}", path.getString("routeHandler"));
            Runtime.getRuntime().exit(10);
        }
        return null;
    }
    
    /**
     * Gets a file from the .jar files loaded at initialization of SWB. The
     * requested file's path has to be within the structure directory of
     * SWBAdmin.jar or dojo.jar.
     * <p>
     * Extrae un archivo de los archivos .jar cargados en la
     * inicializaci&oacute;n de SWB. La ruta de archivo solicitada debe existir
     * en la estructura de directorios de SWBAdmin.jar o dojo.jar.</p>
     *
     * @param path a string representing an existing path within SWBAdmin.jar or
     * dojo.jar files.
     * @return a jarFile object with the specified file's content.
     */
    private JarFile getStaticFile(String path) {
        JarFile f = (JarFile) admFiles.get(path);
        if (f != null) {
            JarFile aux = null;

            if (f.isReplaced()) {
                aux = new JarFile(path);
            } else {
                aux = f;
            }

            if (f.isExpired()) {
                JarFile aux2 = new JarFile(path);
                if (aux2.exists()) {
                    f.setReplaced(true);
                    aux = aux2;
                } else {
                    if (f.isReplaced()) {
                        f.setReplaced(false);
                    }
                }
                f.touch();
            }
            f = aux;
        } else {
            f = new JarFile(path);
        }
        return f;
    }
    
    private boolean processStaticFile(String path, HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        //System.out.println("path:"+path);
        if(path.endsWith(".jsp"))return false;
        
        JarFile file = getStaticFile(path);
        if(!file.exists())
        {
            return false;
        }
        
        if(!file.isDirectory() && path.endsWith("/"))
        {
            //response.sendError(404, path);
            return false;
        }
        
        if(!file.isDirectory() && !checkIfHeaders(request, response, file))
        {
            //System.out.println("checkIfHeaders:"+true);
            return true;
        }

        boolean compress=path.endsWith(".gz");
        String contentType = request.getSession().getServletContext().getMimeType(compress?path.substring(0,path.length()-3):path);
        
        if(contentType==null)contentType="bin/application";
        if(file.isDirectory())
        {
            return false;
        } else
        {
            response.setHeader("ETag", getETag(file));
            response.setDateHeader("Last-Modified", file.lastModified());
        }
        
        //System.out.println("process:"+path);
        
        response.setContentType(contentType);
        if(compress)response.setHeader("Content-Encoding", "gzip");

        boolean gzip = false;
        if (agzip)
        {
            if (request.getHeader("Via") != null
                    || request.getHeader("X-Forwarded-For") != null
                    //|| request.getHeader("Cache-Control") != null
                )
            {
                //using proxy -> no zip
            } else {
                String accept = request.getHeader("Accept-Encoding");
                if (accept != null && accept.toLowerCase().indexOf("gzip") != -1) {
                    gzip = true;
                }
            }
        }

        java.util.zip.GZIPOutputStream garr = null;
        OutputStream out=null;

        if (gzip && contentType.indexOf("text")>-1) {
            response.setHeader("Content-Encoding", "gzip");
            out = new java.util.zip.GZIPOutputStream(response.getOutputStream());
        } else {
            response.setContentLength((int)file.length());
            out = response.getOutputStream();
        }
        
        try
        {
            response.setBufferSize(8192);
        }catch(Exception noe){}

        if(file.hasCache())
        {
            out.write(file.getCache());
            out.flush();
            out.close();
        }else
        {
            DataUtils.copyStream(file.getInputStream(), out);
        }        
        return true;
    }
    
    /**
     * Check if headers.
     * 
     * @param request the request
     * @param response the response
     * @param file the file
     * @return true, if successful
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private boolean checkIfHeaders(HttpServletRequest request, HttpServletResponse response, JarFile file)
        throws IOException
    {
        String eTag = getETag(file);
        long fileLength = file.length();
        long lastModified = file.lastModified();
        String headerValue = request.getHeader("If-Match");
        if(headerValue != null && headerValue.indexOf('*') == -1)
        {
            StringTokenizer commaTokenizer = new StringTokenizer(headerValue, ",");
            boolean conditionSatisfied;
            for(conditionSatisfied = false; !conditionSatisfied && commaTokenizer.hasMoreTokens();)
            {
                String currentToken = commaTokenizer.nextToken();
                if(currentToken.trim().equals(eTag))
                    conditionSatisfied = true;
            }

            if(!conditionSatisfied)
            {
                response.sendError(412);
                return false;
            }
        }
        headerValue = request.getHeader("If-Modified-Since");
        if(headerValue != null && request.getHeader("If-None-Match") == null)
        {
            Date date = null;
            for(int i = 0; date == null && i < dateFormats.length; i++)
                try
                {
                    date = dateFormats[i].parse(headerValue);
                }
                catch(Exception e) { }
            if(date != null && lastModified <= date.getTime() + 1000L)
            {
                response.setStatus(304);
                return false;
            }
        }
        headerValue = request.getHeader("If-None-Match");
        if(headerValue != null)
        {
            boolean conditionSatisfied = false;
            if(!headerValue.equals("*"))
            {
                for(StringTokenizer commaTokenizer = new StringTokenizer(headerValue, ","); !conditionSatisfied && commaTokenizer.hasMoreTokens();)
                {
                    String currentToken = commaTokenizer.nextToken();
                    if(currentToken.trim().equals(eTag))
                        conditionSatisfied = true;
                }

            } else
            {
                conditionSatisfied = true;
            }
            if(conditionSatisfied)
                if("GET".equals(request.getMethod()) || "HEAD".equals(request.getMethod()))
                {
                    response.setStatus(304);
                    return false;
                } else
                {
                    response.sendError(412);
                    return false;
                }
        }
        headerValue = request.getHeader("If-Unmodified-Since");
        if(headerValue != null)
        {
            Date date = null;
            for(int i = 0; date == null && i < dateFormats.length; i++)
                try
                {
                    date = dateFormats[i].parse(headerValue);
                }
                catch(java.text.ParseException e) { }

            if(date != null && lastModified > date.getTime())
            {
                response.sendError(412);
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the e tag.
     * 
     * @param file the file
     * @return the e tag
     */
    private String getETag(JarFile file)
    {
        return "\"" + file.length() + "-" + file.lastModified() + "\"";
    }            
    

}
