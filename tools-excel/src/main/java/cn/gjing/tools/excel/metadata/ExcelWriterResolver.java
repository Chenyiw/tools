package cn.gjing.tools.excel.metadata;

import cn.gjing.tools.excel.exception.ExcelResolverException;
import cn.gjing.tools.excel.write.BigTitle;
import cn.gjing.tools.excel.write.ExcelWriterContext;
import cn.gjing.tools.excel.write.callback.ExcelAutoMergeCallback;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Excel writer resolver
 *
 * @author Gjing
 **/
public abstract class ExcelWriterResolver {
    /**
     * Init resolver
     *
     * @param context Excel write context
     */
    public abstract void init(ExcelWriterContext context);

    /**
     * Write excel big title
     *
     * @param bigTitle Excel big title
     */
    public abstract void writeTitle(BigTitle bigTitle);

    /**
     * Write excel body
     *
     * @param data data
     */
    public abstract void write(List<?> data);

    /**
     * Write excel body
     *
     * @param callbackCache Merge callbacks at export time
     * @param data          Exported data
     * @param mergeEmpty    Whether null data is allowed to initiate a merge callback
     */
    public abstract void simpleWrite(List<List<Object>> data, boolean mergeEmpty, Map<String, ExcelAutoMergeCallback<?>> callbackCache);

    /**
     * Write excel header
     *
     * @param needHead  Is needHead excel entity or sheet?
     * @param boxValues Excel dropdown box values
     * @return this
     */
    public abstract ExcelWriterResolver writeHead(boolean needHead, Map<String, String[]> boxValues);

    /**
     * Write excel header
     *
     * @param needHead Is needHead excel entity or sheet?
     * @return this
     */
    public abstract ExcelWriterResolver simpleWriteHead(boolean needHead);

    /**
     * Output the contents of the cache
     *
     * @param context  Excel write context
     * @param response response
     */
    public void flush(HttpServletResponse response, ExcelWriterContext context) {
        response.setContentType("application/vnd.ms-excel");
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        OutputStream outputStream = null;
        try {
            if (request.getHeader("User-Agent").toLowerCase().indexOf("firefox") > 0) {
                context.setFileName(new String(context.getFileName().getBytes(StandardCharsets.UTF_8), "ISO8859-1"));
            } else {
                context.setFileName(URLEncoder.encode(context.getFileName(), "UTF-8"));
            }
            response.setHeader("Content-disposition", "attachment;filename=" + context.getFileName() + (context.getExcelType() == ExcelType.XLS ? ".xls" : ".xlsx"));
            outputStream = response.getOutputStream();
            context.getWorkbook().write(outputStream);
        } catch (IOException e) {
            throw new ExcelResolverException("Excel cache data flush failure, " + e.getMessage());
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
                if (context.getWorkbook() != null) {
                    context.getWorkbook().close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Output the contents of the cache to local
     *
     * @param path    Absolute path to the directory where the file is stored
     * @param context Excel write context
     */
    public void flushToLocal(String path, ExcelWriterContext context) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream((path.endsWith("/") ? path : path + "/") + context.getFileName() + (context.getExcelType() == ExcelType.XLS ? ".xls" : ".xlsx"));
            context.getWorkbook().write(fileOutputStream);
        } catch (IOException e) {
            throw new ExcelResolverException("Excel cache data flush failure, " + e.getMessage());
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
                if (context.getWorkbook() != null) {
                    context.getWorkbook().close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
