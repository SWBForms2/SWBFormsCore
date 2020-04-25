/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.semanticwb.swbforms.authentication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import org.semanticwb.datamanager.DataList;
import org.semanticwb.datamanager.DataMgr;
import org.semanticwb.datamanager.DataObject;
import org.semanticwb.datamanager.SWBDataSource;
import org.semanticwb.datamanager.SWBScriptEngine;

/**
 *
 * @author juan.fernandez
 */
public class UserLogin {

    static SWBScriptEngine eng = null;

    /**
     * UserLogin Constructor
     *
     * @param eng SWBScriptEngine
     */
    public UserLogin(SWBScriptEngine eng) {
        this.eng = eng;
    }

    /**
     * Check user Credentials
     *
     * @param userEmail a User Email or UserName
     * @param pass User Password
     * @return a User DataObject or NULL
     */
    public DataObject getUserByEmail(String userEmail, String pass) {
        // check login config
        DataObject config = eng.getConfigData();
//        System.out.println("CONFIG:\n" + config);
//        System.out.println("context path:\n\n" + DataMgr.getContextPath());
//        System.out.println("APP-PATH..." + DataMgr.getApplicationPath());
        DataObject user = null;
        if (null != config && config.getString("AUTH_TYPE", "DEFAULT").equals("LDAP")) {  // finding user in Active Directory / LDAP
//            System.out.println("LDAP CONFIG .... ");
            DataObject objldap = config.getDataObject("LDAP");
            if (null != objldap) {
//                System.out.println("DATAOBJECT:" + objldap);
                LdapContext ldapContext = getLdapContext(userEmail, pass, objldap);
//                System.out.println("BEFORE ... search controls....");
                if(null==ldapContext) return user;
                //Check if user exists in the DB....
                // check user by email
                SWBDataSource ds = eng.getDataSource("User");
                DataObject r = new DataObject();
                DataObject data = new DataObject();
                r.put("data", data);
                data.put("email", userEmail);
                try {
                    DataObject res = ds.fetch(r);
                    DataList rdata = res.getDataObject("response").getDataList("data");
                    if (!rdata.isEmpty()) {
                        user = rdata.getDataObject(0); // Ya se encuentra registrado
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(null==user){
                    SearchControls searchControls = getSearchControls(objldap);
    //                System.out.println("AFTER .... ");
                    String tmp_userName = userEmail;
                    if (userEmail.contains("@")) {
                        tmp_userName = userEmail.substring(0, userEmail.indexOf("@"));
                    }
                    user = synchUser(getUserInfo(tmp_userName, ldapContext, searchControls, objldap), objldap);
                }
            }
        } else {
            if (userEmail != null && pass != null) {  // finding user in local DB
                SWBDataSource ds = eng.getDataSource("User");
                DataObject r = new DataObject();
                DataObject data = new DataObject();
                r.put("data", data);
                data.put("email", userEmail);
                data.put("password", pass);
                try {
                    DataObject ret = ds.fetch(r);
                    DataList rdata = ret.getDataObject("response").getDataList("data");
                    if (!rdata.isEmpty()) {
                        user = rdata.getDataObject(0);
                    }
                } catch (Exception e) {
                }
            }
        }
        return user;
    }

    /**
     * Add user to local DB if not exists
     *
     * @param ldapuser user info fond from LDAP
     * @return user DataObject with the user information
     */
    public DataObject synchUser(DataObject ldapuser, DataObject ldapConfig) {
        DataObject retUsr = null;
        // check user by email
        SWBDataSource ds = eng.getDataSource("User");
        DataObject r = new DataObject();
        DataObject data = new DataObject();
        r.put("data", data);
        data.put("email", ldapuser.getString("email"));
        try {
            DataObject res = ds.fetch(r);
            DataList rdata = res.getDataObject("response").getDataList("data");
            if (!rdata.isEmpty()) {
                retUsr = rdata.getDataObject(0); // Ya se encuentra registrado
            } else {
                //Add LDAP user to DB
                DataList dlroles = new DataList();
                dlroles.add("user");
                ldapuser.addParam("roles", dlroles);
                res = ds.addObj(ldapuser);
                rdata = res.getDataObject("response").getDataList("data");
                if (!rdata.isEmpty()) {
                    retUsr = rdata.getDataObject(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return retUsr;
    }

    /**
     * Check user credentials into LDAP
     *
     * @param username User name to check
     * @param pass User Password
     * @param ldapConfig DataObject with the LDAP Configuration
     * @return a LDAP context if the user exists
     */
    private static LdapContext getLdapContext(String username, String pass, DataObject ldapConfig) {
        LdapContext ctx = null;
        try {
            Hashtable<String, String> env = new Hashtable<String, String>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, ldapConfig.getString("INITIAL_CONTEXT_FACTORY", "com.sun.jndi.ldap.LdapCtxFactory"));
            env.put(Context.SECURITY_AUTHENTICATION, ldapConfig.getString("SECURITY_AUTHENTICATION", "Simple"));
            env.put(Context.SECURITY_PRINCIPAL, username);//input user & password for access to ldap
            env.put(Context.SECURITY_CREDENTIALS, pass);
            env.put(Context.PROVIDER_URL, ldapConfig.getString("PROVIDER_URL", "ldap://192.168.97.22"));
            env.put(Context.REFERRAL, ldapConfig.getString("REFERRAL", "follow"));
            ctx = new InitialLdapContext(env, null);

//            System.out.println("LDAP Connection: COMPLETE");
        } catch (NamingException nex) {
//            System.out.println("LDAP Connection: FAILED");
            nex.printStackTrace();
        }
        return ctx;
    }

    /**
     * Obtains the user information from LDAP
     *
     * @param userName a User Name into the LDAP to get the information
     * @param ctx LDAP Context
     * @param searchControls
     * @param ldapConfig
     * @return
     */
    private static DataObject getUserInfo(String userName, LdapContext ctx, SearchControls searchControls, DataObject ldapConfig) {  //Return USER originalmente
//        System.out.println("*** " + userName + " ***");
        DataObject user = null;
        String SEARCH_NAME = ldapConfig.getString("SEARCH_NAME", "DC=infotec,DC=mx");
        String OBJECT_CLASS = ldapConfig.getString("OBJECT_CLASS", "person");
        try {
            NamingEnumeration<SearchResult> answer = ctx.search(SEARCH_NAME, "(&(objectClass=" + OBJECT_CLASS + ")(sAMAccountName=" + userName + "))", searchControls);
            if (answer.hasMore()) {
//                System.out.println("BEFORE...");
                Attributes attrs = answer.next().getAttributes();

                // Create a new User DataObject with the user info from LDAP
                user = new DataObject();
                //user.addParam("distinguishedName",getAttrValue(attrs.get("distinguishedName").toString()));

                user.addParam("fullname", getAttrValue(attrs.get(ldapConfig.getString("USER_FULLNAME")).toString()));
                user.addParam("email", getAttrValue(attrs.get(ldapConfig.getString("USER_EMAIL")).toString()));
                user.addParam("name", getAttrValue(attrs.get(ldapConfig.getString("USER_NAME")).toString()));
                user.addParam("surname", getAttrValue(attrs.get(ldapConfig.getString("USER_SNAME")).toString()));
                user.addParam("lastName", getAttrValue(attrs.get(ldapConfig.getString("USER_LASTNAME")).toString()));
                user.addParam("secondLastName", getAttrValue(attrs.get(ldapConfig.getString("USER_SECONDLASTNAME")).toString()));

//                user.addParam("title",getAttrValue(attrs.get("title").toString()));
//                user.addParam("description",getAttrValue(attrs.get("description").toString()));
//                user.addParam("initials",getAttrValue(attrs.get("initials").toString()));
//                user.addParam("department",getAttrValue(attrs.get("department").toString()));
//                user.addParam("displayName",getAttrValue(attrs.get("displayName").toString()));
//                user.addParam("st",getAttrValue(attrs.get("st").toString()));
//                user.addParam("telephoneNumber",getAttrValue(attrs.get("telephoneNumber").toString()));
                if (ldapConfig.containsKey("USER_EXTATTR1")) {
                    user.addParam(ldapConfig.getString("USER_EXTATTR1"), getAttrValue(attrs.get(ldapConfig.getString("USER_EXTATTR1")).toString()));
                }
                if (ldapConfig.containsKey("USER_EXTATTR2")) {
                    user.addParam(ldapConfig.getString("USER_EXTATTR2"), getAttrValue(attrs.get(ldapConfig.getString("USER_EXTATTR2")).toString()));
                }
                if (ldapConfig.containsKey("USER_EXTATTR3")) {
                    user.addParam(ldapConfig.getString("USER_EXTATTR3"), getAttrValue(attrs.get(ldapConfig.getString("USER_EXTATTR3")).toString()));
                }
                if (ldapConfig.containsKey("USER_EXTATTR4")) {
                    user.addParam(ldapConfig.getString("USER_EXTATTR4"), getAttrValue(attrs.get(ldapConfig.getString("USER_EXTATTR4")).toString()));
                }
                if (ldapConfig.containsKey("USER_EXTATTR5")) {
                    user.addParam(ldapConfig.getString("USER_EXTATTR5"), getAttrValue(attrs.get(ldapConfig.getString("USER_EXTATTR5")).toString()));
                }

                if (ldapConfig.containsKey("USER_THUMBNAIL")) {
//                    System.out.println("Revisando PHOTO.....\n"+DataMgr.getContextPath()+"/uploadfile/");
                    byte[] photo = (byte[]) attrs.get(ldapConfig.getString("USER_THUMBNAIL")).get();
                    getUserPhoto(userName, photo);
                    user.addParam("photo", DataMgr.getContextPath() + "/uploadfile/" + userName + ".jpg");
                }
            } else {
                System.out.println("User not found.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return user;
    }

    private static String getAttrValue(String value) {
        if (null != value) {
            if (value.contains(":")) {
                return value.substring(value.indexOf(":") + 1).trim();
            }
        }
        return null;
    }

    /**
     * Serch User LDAP Attributes
     *
     * @param ldapConfig LDAP configuration
     * @return a list of attributtes to get from LDAP
     */
    private static SearchControls getSearchControls(DataObject ldapConfig) {
        SearchControls cons = new SearchControls();
        cons.setSearchScope(SearchControls.SUBTREE_SCOPE);
        List<String> list = new ArrayList<>();

        if (ldapConfig.containsKey("USER_FULLNAME")) {
            list.add(ldapConfig.getString("USER_FULLNAME"));
        }
        if (ldapConfig.containsKey("USER_SNAME")) {
            list.add(ldapConfig.getString("USER_SNAME"));
        }
        if (ldapConfig.containsKey("USER_NAME")) {
            list.add(ldapConfig.getString("USER_NAME"));
        }
        if (ldapConfig.containsKey("USER_LASTNAME")) {
            list.add(ldapConfig.getString("USER_LASTNAME"));
        }
        if (ldapConfig.containsKey("USER_SECONDLASTNAME")) {
            list.add(ldapConfig.getString("USER_SECONDLASTNAME"));
        }
        if (ldapConfig.containsKey("USER_EMAIL")) {
            list.add(ldapConfig.getString("USER_EMAIL"));
        }
        if (ldapConfig.containsKey("USER_THUMBNAIL")) {
            list.add(ldapConfig.getString("USER_THUMBNAIL"));
        }

//        list.add("distinguishedName");
//        list.add("sn");
//        list.add("givenName");
//        list.add("title");
//        list.add("description");
//        list.add("initials");
//        list.add("department");
//        list.add("displayName");
//        list.add("mail");
//        list.add("thumbnailPhoto");
//        list.add("sAMAccountName");
//        list.add("cn");
//        list.add("name");
//        list.add("st");
//        list.add("telephoneNumber");
        // Add extended Attributes
        if (ldapConfig.containsKey("USER_EXTATTR1")) {
            list.add(ldapConfig.getString("USER_EXTATTR1"));
        }
        if (ldapConfig.containsKey("USER_EXTATTR2")) {
            list.add(ldapConfig.getString("USER_EXTATTR2"));
        }
        if (ldapConfig.containsKey("USER_EXTATTR3")) {
            list.add(ldapConfig.getString("USER_EXTATTR3"));
        }
        if (ldapConfig.containsKey("USER_EXTATTR4")) {
            list.add(ldapConfig.getString("USER_EXTATTR4"));
        }
        if (ldapConfig.containsKey("USER_EXTATTR5")) {
            list.add(ldapConfig.getString("USER_EXTATTR5"));
        }
        String[] attrIDs = new String[list.size()];
        int num = 0;
        for (String str : list) {
//            System.out.println("attr==>"+str);
            attrIDs[num] = str;
            num++;
        }
        cons.setReturningAttributes(attrIDs);
        return cons;
    }

    /**
     * Util to save the User Thumbnail from LDAP
     *
     * @param userName User-Name
     * @param photo a User photo byte array
     * @throws IOException
     */
    private static void getUserPhoto(String userName, byte[] photo) throws IOException {
//        System.out.println("SAVE IMAGE...."+photo.length+"===>"+photo.toString());
        try {
            File f = new File(DataMgr.getApplicationPath() + "/uploadfile/");
            if (!f.exists()) {
                f.mkdirs();
            }
            File fth = new File(DataMgr.getApplicationPath() + "/uploadfile/" + userName + ".jpg");
            if (!f.exists()) {
                FileOutputStream os = new FileOutputStream(DataMgr.getApplicationPath() + "/uploadfile/" + userName + ".jpg");
                os.write(photo);
                os.flush();
                os.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
