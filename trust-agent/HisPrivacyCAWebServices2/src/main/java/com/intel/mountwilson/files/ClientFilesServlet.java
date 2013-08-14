/*
 * Copyright (c) 2013, Intel Corporation. 
 * All rights reserved.
 * 
 * The contents of this file are released under the BSD license, you may not use this file except in compliance with the License.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of Intel Corporation nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.files;

import com.intel.mtwilson.util.ResourceFinder;
import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dsmagadx
 */
public class ClientFilesServlet extends HttpServlet {
    private final Logger log = LoggerFactory.getLogger(getClass()); 
    private String username = null;
    private String password = null;
    public ClientFilesServlet() {
        super();
        try {
            File configFile = ResourceFinder.getFile("PrivacyCA.properties");
            FileInputStream in = new FileInputStream(configFile);
            Properties p = new Properties();
            p.load(in);
            username = p.getProperty("ClientFilesDownloadUsername");
            password = p.getProperty("ClientFilesDownloadPassword");
            in.close();
        }
        catch(Exception e) {
            log.info("Error while loading PrivacyCA.properties: "+e.getMessage());
        }
        finally {
            if( username == null || password == null || password.isEmpty() ) {
                log.info("Download username and password not set; client files download disabled");
            }            
        }
    }
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        log.info("Client Files request called.");
        String user = request.getParameter("user");
        String pwd = request.getParameter("password");
        if (username != null && password != null && !password.isEmpty() 
                && user != null && user.equals(username) && pwd != null && pwd.equals(password)) {


            String setUpFile = ResourceFinder.getFile("privacyca-client.properties").getAbsolutePath();
            String fileLocation = setUpFile.substring(0, setUpFile.indexOf("privacyca-client.properties"));

            log.info("Path :" + fileLocation + "clientfiles.zip");
            File clientFiles = new File(fileLocation + "clientfiles.zip");

            if (clientFiles.exists()) {
                OutputStream os = null;
                try {
                    response.setContentType("application/x-zip-compressed");
                    InputStream is = new FileInputStream(ResourceFinder.getFile("clientfiles.zip"));

                    int read = 0;
                    byte[] bytes = new byte[1024];
                    os = response.getOutputStream();
                    while ((read = is.read(bytes)) != -1) {
                        os.write(bytes, 0, read);
                    }
                } finally {
                    if (os != null) {
                        os.flush();
                        os.close();
                    }
                }
            } else {
                PrintWriter out = response.getWriter();
                response.setContentType("application/html");

                out.write("File not created. Try again later.");
                log.info("File not created. Try again later.");
            }
        }
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
        return "File Download Servlet.";
    }// </editor-fold>
}
