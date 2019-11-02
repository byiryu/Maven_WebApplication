package kr.byiryu.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import kr.byiryu.db.DBConnector;
import kr.byiryu.retrofit.Gson.GsonConverter;
import kr.byiryu.retrofit.ResponseCode;
import kr.byiryu.retrofit.ResponseMessage;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.List;

public class BaseAdapter<T> implements BaseController {

    private final Logger LOG;
    public String BASE_UPLOAD_URL = ".. 서버 업로드 url";

    public Gson gson = new GsonBuilder().serializeNulls().create();
    public JSONParser jsonParser = new JSONParser();
    public String tableName; // database table name.
    private Class<T> typeClass; // bean class
    private T instance;

    public BaseAdapter(Class<T> typeClass, String tableName) {
        this.tableName = tableName;
        this.typeClass = typeClass;
        try {
            this.instance = typeClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        Class<? extends BaseAdapter> cl = this.getClass();
        LOG = LoggerFactory.getLogger(cl.getName());
    }

    public HttpServletRequest getRequest() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes()).getRequest();
        return request;
    }

    public HttpServletResponse getResponse() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = ((ServletRequestAttributes) requestAttributes).getResponse();
        return response;
    }

    @Override
    public String index(Model model) {
        return null;
    }

    @RequestMapping(value = "/get.do")
    @ResponseBody
    @Override
    public ResponseMessage get(@RequestParam Integer idx) {

        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.code = ResponseCode.Success;
        String indexColumnName = getIndexColumnName();
        T table = null;
        ResultSetHandler<T> h = new BeanHandler<T>(typeClass);
        Connection conn = DBConnector.getInstance().login();
        try {
            QueryRunner queryRunner = new QueryRunner();
            String sql = "SELECT * FROM " + tableName + " WHERE " + indexColumnName + " = ?;";
            table = queryRunner.query(conn, sql, h, idx);
            responseMessage.entity = jsonParser.parse(gson.toJson(table));
        } catch (Exception se) {
            responseMessage.code = ResponseCode.Error;
        } finally {
            DbUtils.closeQuietly(conn);
        }

        if (table == null)
            responseMessage.code = ResponseCode.Null;




        return responseMessage;
    }

    @RequestMapping(value = "/list.do")
    @ResponseBody
    @Override
    public ResponseMessage list() {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.code = ResponseCode.Success;
        List<T> table = null;
        ResultSetHandler<List<T>> h = new BeanListHandler<>(typeClass);
        Connection conn = DBConnector.getInstance().login();
        try {
            QueryRunner queryRunner = new QueryRunner();
            String sql = "SELECT * FROM " + tableName + ";";
            table = queryRunner.query(conn, sql, h);
            responseMessage.entity = jsonParser.parse(gson.toJson(table));
        } catch (Exception se) {
            responseMessage.code = ResponseCode.Error;
        } finally {
            DbUtils.closeQuietly(conn);
        }

        if (table == null)
            responseMessage.code = ResponseCode.Null;
//        responseMessage.json = gson.toJson(table);

        return responseMessage;
    }

    @RequestMapping(value = "/list_query.do")
    @ResponseBody
    @Override
    public ResponseMessage list(@RequestParam String column, @RequestParam String value) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.code = ResponseCode.Success;
        Integer n = null;
        if (StringUtils.isNumeric(value)) {
            n = Integer.parseInt(value);
        }
        List<T> table = null;
        ResultSetHandler<List<T>> h = new BeanListHandler<>(typeClass);
        Connection conn = DBConnector.getInstance().login();
        try {
            QueryRunner queryRunner = new QueryRunner();
            String sql = "SELECT * FROM " + tableName + " WHERE " + column + " = ?;";
            if (StringUtils.isNumeric(value)) {
                table = queryRunner.query(conn, sql, h, n);
            } else {
                table = queryRunner.query(conn, sql, h, value);
            }
            responseMessage.entity = jsonParser.parse(gson.toJson(table));
        } catch (Exception se) {
            responseMessage.code = ResponseCode.Error;
        } finally {
            DbUtils.closeQuietly(conn);
        }

        if (table == null)
            responseMessage.code = ResponseCode.Null;
//        responseMessage.json = gson.toJson(table);

        return responseMessage;
    }

    @RequestMapping(value = "/list_offset.do")
    @ResponseBody
    @Override
    public ResponseMessage list(@RequestParam String order, @RequestParam Integer offset, @RequestParam Integer limit) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.code = ResponseCode.Success;
        List<T> table = null;
        ResultSetHandler<List<T>> h = new BeanListHandler<>(typeClass);
        Connection conn = DBConnector.getInstance().login();
        try {
            QueryRunner queryRunner = new QueryRunner();
            String sql = "SELECT * FROM " + tableName + " ORDER BY ? LIMIT ?, ?";
            table = queryRunner.query(conn, sql, h, order, offset, limit);
            responseMessage.entity = jsonParser.parse(gson.toJson(table));
        } catch (Exception se) {
            responseMessage.code = ResponseCode.Error;
        } finally {
            DbUtils.closeQuietly(conn);
        }

        if (table == null)
            responseMessage.code = ResponseCode.Null;
//        responseMessage.json = gson.toJson(table);

        return responseMessage;
    }

    @RequestMapping(value = "/insert.do", consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = {RequestMethod.POST})
    @ResponseBody
    @Override
    public ResponseMessage insert(@RequestBody String beanList) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.code = ResponseCode.Success;
        List<T> table = GsonConverter.toList(beanList, typeClass);
        Connection conn = DBConnector.getInstance().login();
        String values = getInserts(table);
        String columns = getInsertColumns(instance);
        try {
            QueryRunner queryRunner = new QueryRunner();
            String sql = "INSERT INTO " + tableName + columns + " VALUES " + values;
            queryRunner.update(conn, sql);
        } catch (Exception se) {
            se.printStackTrace();
            responseMessage.code = ResponseCode.Error;
        } finally {
            DbUtils.closeQuietly(conn);
        }

        if (table == null)
            responseMessage.code = ResponseCode.Null;
        responseMessage.entity = list().entity;

        return responseMessage;
    }

    @RequestMapping(value = "/update.do", consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = {RequestMethod.POST})
    @ResponseBody
    @Override
    public ResponseMessage update(@RequestBody String beanList) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.code = ResponseCode.Success;
        List<T> table = GsonConverter.toList(beanList, typeClass);
        Connection conn = DBConnector.getInstance().login();
        try {
            QueryRunner queryRunner = new QueryRunner();
            for (int i = 0; i < table.size(); i++) {
                T t = table.get(i);
                String sql = "UPDATE " + tableName + " SET " + getUpdateQuery(t);
                System.out.println(sql);
                queryRunner.update(conn, sql);
            }

        } catch (Exception se) {
            se.printStackTrace();
            responseMessage.code = ResponseCode.Error;
        } finally {
            DbUtils.closeQuietly(conn);
        }

        if (table == null)
            responseMessage.code = ResponseCode.Null;
        responseMessage.entity = list().entity;

        return responseMessage;
    }

    @RequestMapping(value = "/delete.do", consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = {RequestMethod.POST})
    @ResponseBody
    @Override
    public ResponseMessage delete(@RequestBody String beanList) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.code = ResponseCode.Success;
        List<T> table = GsonConverter.toList(beanList, typeClass);
        Connection conn = DBConnector.getInstance().login();
        try {
            QueryRunner queryRunner = new QueryRunner();
            String sql = "DELETE FROM " + tableName + " WHERE " + getDeleteQuery(table);
            queryRunner.update(conn, sql);

        } catch (Exception se) {
            se.printStackTrace();
            responseMessage.code = ResponseCode.Error;
        } finally {
            DbUtils.closeQuietly(conn);
        }

        if (table == null)
            responseMessage.code = ResponseCode.Null;
//        responseMessage.json = list().json;
        responseMessage.entity = list().entity;

        return responseMessage;
    }

    public ResponseMessage getListPostgre(String sql, Object[] objs, String columnName, List<String> statusList) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.code = ResponseCode.Success;

        if(statusList != null)
            statusList.removeIf(s -> s.trim().length() < 1);

        if((columnName != null && statusList != null) || (columnName != null && statusList == null))
            if (statusList == null || (statusList != null && statusList.size() == 0)) {
                responseMessage.code = ResponseCode.Null;
                responseMessage.json = "[]";
                return responseMessage;
            }

        List<T> table = null;
        ResultSetHandler<List<T>> h = new BeanListHandler<>(typeClass);
        Connection conn = DBConnector.getInstance().login();
        try {
            String status = getSqlStatus(statusList, columnName);
            if(status.length() > 0){
                sql += status;
            }
            sql += ";";
            System.out.println(sql);
            QueryRunner queryRunner = new QueryRunner();
            if (objs == null)
                table = queryRunner.query(conn, sql, h);
            else
                table = queryRunner.query(conn, sql, h, objs);
            responseMessage.entity = jsonParser.parse(gson.toJson(table));
        } catch (Exception se) {
            se.printStackTrace();
            responseMessage.code = ResponseCode.Error;
        } finally {
            DbUtils.closeQuietly(conn);
        }

        if (table == null)
            responseMessage.code = ResponseCode.Null;
//        responseMessage.json = gson.toJson(table);

        return responseMessage;
    }

    public String getSqlStatus(List<String> list, String columnName) {
        String status = "";
        if(list != null){
            if (!list.contains("전체")) {
                String join = String.join("', '", list);
                if (join.length() > 0)
                    status = " AND LOWER(" + columnName + ") IN ('" + join + "')";
            }
        }
        return status;
    }

    public String getIndexColumnName() {
        List<String> table = null;
        ColumnListHandler<String> h = new ColumnListHandler<String>(1);
        Connection conn = DBConnector.getInstance().login();
        try {
            QueryRunner queryRunner = new QueryRunner();
            String sql = "SELECT column_name FROM information_schema.columns WHERE table_name = ? LIMIT 1; ";
            table = queryRunner.query(conn, sql, h, tableName);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(conn);
        }
        String columnName = null;
        if (table != null && table.size() > 0) {
            columnName = table.get(0);
        }

        return columnName;
    }

    public String getInserts(List<T> table) {
        String values = "";
        int tSize = table.size();

        if (tSize == 1)
            return getInsertValues(table.get(0));
        for (int i = 0; i < tSize; i++) {
            values += getInsertValues(table.get(i));
            if (i != tSize - 1) {
                values += ", ";
            }
        }
        values += ";";
        return values;
    }

    public String getInsertValues(T t) {
        StringBuilder sb = new StringBuilder();

        Class<?> thisClass = null;
        try {
            thisClass = Class.forName(t.getClass().getName());

            Field[] aClassFields = thisClass.getDeclaredFields();
            sb.append("(");
            for (int i = 0; i < aClassFields.length; i++) {
                Field f = aClassFields[i];
                f.setAccessible(true);
                Object obj = f.get(t);
                if (obj == null) {
                    sb.append("null");
                } else {
                    if (obj instanceof Integer) {
                        sb.append(obj);
                    } else {
                        sb.append("'" + obj + "'");
                    }
                }
                if (i != aClassFields.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append(")");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    public String getInsertColumns(T t) {
        StringBuilder sb = new StringBuilder();

        Class<?> thisClass = null;
        try {
            thisClass = Class.forName(t.getClass().getName());

            Field[] aClassFields = thisClass.getDeclaredFields();
            sb.append("(");
            for (int i = 0; i < aClassFields.length; i++) {
                Field f = aClassFields[i];
                f.setAccessible(true);
                String fName = f.getName();
                sb.append(fName);
                if (i != aClassFields.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append(")");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    public String getUpdateQuery(T t) {
        StringBuilder sb = new StringBuilder();

        Class<?> thisClass = null;
        try {
            thisClass = Class.forName(t.getClass().getName());
            Field[] aClassFields = thisClass.getDeclaredFields();

            String idColumn = null;
            Integer idValue = null;
            for (int i = 0; i < aClassFields.length; i++) {
                Field f = aClassFields[i];
                f.setAccessible(true);

                String column = f.getName();
                Object value = f.get(t);
                if (value instanceof Integer) {
                    if (column.startsWith("id") && idColumn == null) {
                        idValue = (Integer) value;
                        idColumn = column;
                    }
                    sb.append(column + " = " + value);
                } else
                    sb.append(column + " = '" + value + "'");
                if (i != aClassFields.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("WHERE " + idColumn + " = " + idValue + ";");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    public String getDeleteQuery(List<T> table) {
        StringBuilder sb = new StringBuilder();
        String idColumn = null;
        Class<?> thisClass = null;
        try {
            sb.append("(");

            for (int i = 0; i < table.size(); i++) {
                T t = table.get(i);
                thisClass = Class.forName(t.getClass().getName());
                Field[] aClassFields = thisClass.getDeclaredFields();

                Integer idValue = null;
                for (int j = 0; j < aClassFields.length; j++) {
                    Field f = aClassFields[j];
                    f.setAccessible(true);

                    String column = f.getName();
                    Object value = f.get(t);
                    if (value instanceof Integer) {
                        if (column.startsWith("id")) {
                            idValue = (Integer) value;
                            idColumn = column;
                            break;
                        }
                    }
                }
                if (idValue != null) {
                    sb.append(idValue);
                    if (i != table.size() - 1) {
                        sb.append(", ");
                    }
                }

            }
            sb.append(")");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return idColumn + " in " + sb.toString();
    }
}
