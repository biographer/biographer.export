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
 * This servlet can be used 
 * 
 * @author Ben Ripkens <bripkens.dev@gmail.com>
 */
@WebServlet(name = "Transcoder",
            urlPatterns = {"/transcode"})
public class Transcoder extends HttpServlet {

    /**
     * URL request parameter which holds the target format
     */
    private static final String FORMAT_PARAMETER = "format";
    
    /**
     * URL request parameter which holds the input SVG
     */
    private static final String DATA_PARAMETER = "data";
    
    /**
     * This date format is used for the generation of a file name.
     */
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
    
    /**
     * Retrieve the target export format for the given request.
     * 
     * @param req The request for which the format shall be identified
     * @return The format which was set through the format request parameter.
     * If no format could be identified null will be returned.
     */
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
    
    /**
     * Set HTTP headers for the given format.
     * 
     * @param format The format for which the headers shall be set.
     * @param resp The HTTP response for which the headers should be set.
     */
    private void setHeaders(Format format, HttpServletResponse resp) {
        String header =  "attachment;filename=\""
                .concat(getFileName(format))
                .concat("\"");
        
        resp.setHeader("Content-Disposition", header);
        resp.setContentType(format.getContentType());
    }
    
    /**
     * Generate a filename based on the current time and target format.
     */
    private String getFileName(Format format) {
        return new StringBuilder()
                .append("diagram_")
                .append(DATE_FORMAT.format(new Date()))
                .append(".")
                .append(format.getFileNameExtension())
                .toString();
    }
    
    /**
     * Actually start transcoding the input SVG to the target format.
     * 
     * @param format The target format for the export process
     * @param data The SVG which should be converted to the format
     * @param resp HTTP response through which the output stream can be
     * retrieved
     * @throws IOException 
     */
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
