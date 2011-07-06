package de.huberlin.exporter;

import de.bripkens.svgexport.Format;
import de.bripkens.svgexport.SVGExport;
import de.bripkens.svgexport.SVGExportException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Ben Ripkens <bripkens.dev@gmail.com>
 */
@WebServlet(name = "Transcoder",
            urlPatterns = {"/transcode"})
public class Transcoder extends HttpServlet {

    private static final String FORMAT_PARAMETER = "format";
    private static final String DATA_PARAMETER = "data";
    
    private static final DateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd");
    
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest req,
            HttpServletResponse resp) throws ServletException, IOException {
        
        Format format = getFormat(req);
        String data = req.getParameter(DATA_PARAMETER);
        
        if (format != null && data != null) {
            setHeaders(format, resp);
            
            try {
                transcode(format, data, resp);
            } catch (SVGExportException ex) {
                resp.sendError(500, ex.getMessage());
            } catch (IOException ex) {
                resp.sendError(500, ex.getMessage());
            }
        } else {
            resp.sendError(400, "Illegal parameters. Format and data "
                    + "parameters are required.");
        }
        
        
    }
    
    private Format getFormat(HttpServletRequest req) {
        String format = req.getParameter(FORMAT_PARAMETER);
        
        if (format == null) {
            return null;
        }
        
        try {
            return Format.valueOf(format.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
    
    private void setHeaders(Format format, HttpServletResponse resp) {
        String header =  "attachment;filename=\""
                .concat(getFileName(format))
                .concat("\"");
        
        resp.setHeader("Content-Disposition", header);
        resp.setContentType(format.getContentType());
    }
    
    private String getFileName(Format format) {
        return new StringBuilder()
                .append("diagram_")
                .append(DATE_FORMAT.format(new Date()))
                .append(".")
                .append(format.getFileNameExtension())
                .toString();
    }
    
    private void transcode(Format format, String data,
            HttpServletResponse resp) throws IOException {
        new SVGExport().setInputAsString(data)
                .setOutput(resp.getOutputStream())
                .setTranscoder(format)
                .transcode();
    }
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
