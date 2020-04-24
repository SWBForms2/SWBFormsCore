<%-- 
    Document   : calendar
    Created on : 13 dic. 2019, 12:39:56
    Author     : javiersolis
--%>
<%@page import="org.semanticwb.datamanager.DataMgr"%>
<%@page import="org.semanticwb.datamanager.SWBScriptEngine"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String contextPath = request.getContextPath();
    SWBScriptEngine eng=DataMgr.initPlatform("/admin/ds/admin.js", session);
%>
<!DOCTYPE html>
<html>
    <head>
        <title>Usuarios</title>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        
        <script src="/platform/js/eng.min.js?id=<%=eng.getId()%>" type="text/javascript"></script>           
        <script src="/platform/isomorphic/system/modules/ISC_Core.js.gz?ver=12.0" type="text/javascript"></script>
        <script src="/platform/isomorphic/system/modules/ISC_Foundation.js.gz?ver=12.0" type="text/javascript"></script>
        <script src="/platform/isomorphic/system/modules/ISC_Containers.js.gz?ver=12.0" type="text/javascript"></script>
        <script src="/platform/isomorphic/system/modules/ISC_Grids.js.gz?ver=12.0" type="text/javascript"></script>
        <script src="/platform/isomorphic/system/modules/ISC_Forms.js.gz?ver=12.0" type="text/javascript"></script>
        <script src="/platform/isomorphic/system/modules/ISC_DataBinding.js.gz?ver=12.0" type="text/javascript"></script>
        <script src="/platform/isomorphic/system/modules/ISC_RichTextEditor.js.gz?ver=12.0" type="text/javascript"></script>
        <script src="/platform/isomorphic/system/modules/ISC_Calendar.js.gz?ver=12.0" type="text/javascript"></script>
        <script src="/platform/isomorphic/skins/Tahoe/load_skin.js.gz?ver=12.0" type="text/javascript"></script>
        <script src="/platform/isomorphic/locales/frameworkMessages_es.properties?ver=12.0" type="text/javascript"></script>
        <script src="/platform/plupload/js/plupload.full.min.js" type="text/javascript"></script>                
        <link href="/admin/css/sc_admin.css" rel="stylesheet" type="text/css" />

    </head>
    <body>
      <div>
        <script type="text/javascript">
            eng.initPlatform("/admin/ds/datasources.js", true);
        </script>
        <script type="text/javascript">            

        var calendar=isc.Calendar.create({
            ID: "eventCalendar", 
            autoFetchData: false,
            dataSource: eng.createDataSource("Event")
        });
        
        calendar.fetchPData=function()
        {
            var dateStart;
            var dateEnd;
            
            if(calendar.getCurrentViewName()==="day")
            {
                 dateStart=calendar.chosenDateStart.toISOString();
                 dateEnd=calendar.chosenDateEnd.toISOString();
            }else if(calendar.getCurrentViewName()==="week")
            {
                 dateStart=calendar.chosenWeekStart.toISOString();
                 dateEnd=calendar.chosenWeekEnd.toISOString();
            }else if(calendar.getCurrentViewName()==="month")
            {
                 dateStart=calendar.monthView.data[0].date1.toISOString();
                 dateEnd=(new Date(new Date(calendar.monthView.data[calendar.monthView.data.length-1].date7.getTime()).setHours(24))).toISOString();        
            }
                        
            calendar.fetchData(
                 {
                    $and:[
                            {
                                startDate:{
                                    $gte:dateStart
                                }
                            },{
                                startDate:{
                                    $lt:dateEnd
                                }
                            }
                        ]
                },
                function(){
                    //alert("Hola");
                },
                {
                    startRow:0,
                    endRow:0
                }
            );             
            
        }
        
        calendar.currentViewChanged=function(viewName){
            calendar.fetchPData();
        }        
        
        calendar.dateChanged=function(p1,p2){
            calendar.fetchPData();
        };
        
        calendar.fetchPData();                
                        
        </script>         
      </div>
    </body>
</html>