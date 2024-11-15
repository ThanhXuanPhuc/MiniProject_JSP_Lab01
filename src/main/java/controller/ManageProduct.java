/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.HoaDAO;
import dao.LoaiDAO;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.sql.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import model.Hoa;

/**
 *
 * @author ADMIN
 */
@WebServlet(name = "ManageProduct", urlPatterns = {"/ManageProduct"})
@MultipartConfig
public class ManageProduct extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //kiểm tra thông tin đăng nhập
        HttpSession session = request.getSession();
        if (session.getAttribute("username") == null) 
        {
            request.getRequestDispatcher("login.jsp").forward(request, response);
        }

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        HoaDAO hoaDAO = new HoaDAO();
        LoaiDAO loaiDAO = new LoaiDAO();
        String method = request.getMethod();
        String action = "LIST";
        if (request.getParameter("action") != null) {
            action = request.getParameter("action");
        }

        //xét các trường hợp của các chức năng
        switch (action) {
            case "LIST":
                //trả về giao diện liệt kê danh sách sản phẩm quản trị
                int pageSize = 5;
                int pageIndex = 1;
                if (request.getParameter("page") != null) {
                    pageIndex = Integer.parseInt(request.getParameter("page"));
                }

                //tính tổng số trang có thể có 
                int sumOfPage = (int) Math.ceil((double) hoaDAO.getAll().size() / pageSize);
                request.setAttribute("dsHoa", hoaDAO.getByPage(pageIndex, pageSize));
                request.setAttribute("sumOfPage", sumOfPage);
                request.setAttribute("pageIndex", pageIndex);
                request.getRequestDispatcher("admin/list_product.jsp").forward(request, response);
                break;

            case "ADD":

                if (method.equals("GET")) {
                    //trả về giao diện thêm mới sản phảm
                    request.setAttribute("dsLoai", loaiDAO.getAll()); //Chuyển dữ liệu cho JSP (VIEW)
                    request.getRequestDispatcher("admin/add_product.jsp").forward(request, response);
                } else if (method.equals("POST")) {
                    //xử lý thêm mới san phảm 
                    //B1. lấy thông tin sản phẩm cần thêm
                    String tenhoa = request.getParameter("tenhoa");
                    double gia = Double.parseDouble(request.getParameter("gia"));
                    Part part = request.getPart("hinh");
                    int maloai = Integer.parseInt(request.getParameter("maloai"));
                    //b2. xử lý upload file (ảnh sản phẩm)
                    String realPath = request.getServletContext().getRealPath("assets/images/products");
                    String filename = Paths.get(part.getSubmittedFileName()).getFileName().toString();
                    part.write(realPath + "/" + filename);
                    //b3. Thêm sản phẩm vào csdl
                    Hoa objInsert = new Hoa(0, tenhoa, gia, filename, maloai, new Date(new java.util.Date().getTime()));
                    if (hoaDAO.Insert(objInsert)) {
                        //thông báo thêm thành công
                        request.setAttribute("success", "Thao tác thêm sản phẩm thành công");
                    } else {
                        request.setAttribute("error", "Thao tác thêm sản phẩm thất bại");
                    }
                    //Chuyển tiếp người dùng về action=LIST để liệt kê lại danh sách sản phảm
                    request.getRequestDispatcher("ManageProduct?action=LIST").forward(request, response);
                }
                break;
            case "EDIT":
                if (method.equals("GET")) {
                    //trả về giao diện cập nhật sản phảm
                    int mahoa = Integer.parseInt(request.getParameter("mahoa"));
                    request.setAttribute("hoa", hoaDAO.getById(mahoa)); //Chuyển đối tượng sản phẩm (hoa) cho JSP
                    request.setAttribute("dsLoai", loaiDAO.getAll()); //Chuyển dữ liệu cho JSP (VIEW)
                    request.getRequestDispatcher("admin/edit_product.jsp").forward(request, response);
                } else if (method.equals("POST")) {
                    //Xử lý cập nhật sản phẩm
                    int mahoa = Integer.parseInt(request.getParameter("mahoa"));
                    String tenhoa = request.getParameter("tenhoa");
                    double gia = Double.parseDouble(request.getParameter("gia"));
                    Part part = request.getPart("hinh"); //hình mới
                    int maloai = Integer.parseInt(request.getParameter("maloai"));
                    String filename = request.getParameter("oldImg");
                    //b2. Xử lý upload file (ả sản phẩm nếu có)
                    if (part.getSize() > 0) {
                        String realPath = request.getServletContext().getRealPath("assets/images/products");
                        filename = Paths.get(part.getSubmittedFileName()).getFileName().toString();
                        part.write(realPath + "/" + filename);
                    }
                    //b3. cập nhật sản phẩm vào CSDL 
                    Hoa objUpdate = new Hoa(mahoa, tenhoa, gia, filename, maloai, new Date(new java.util.Date().getTime()));
                    if (hoaDAO.Update(objUpdate)) {
                        //Thông báo thêm thành công
                        request.setAttribute("success", "Thao tác cập nhật sản phẩm thành công");
                    } else {
                        //thông báo thêm thất bại
                        request.setAttribute("error", "Thao tác cập nhật sản phảm thất bại");
                    }
                    //chuyển tiếp người dùng về action=LIST để liệt kê lại danh sách sản phẩm
                    request.getRequestDispatcher("ManageProduct?action=LIST").forward(request, response);
                }
                break;

            case "DELETE":
                //Xử lý xoá sản phẩm
                //B1. lấy mã sản phẩm
                int mahoa = Integer.parseInt(request.getParameter("mahoa"));
                //b2. xoá sản phẩm khỏi csdl
                if (hoaDAO.Delete(mahoa)) {
                    //thông báo xoá thành công
                    request.setAttribute("success", "Thao tác xoá sản phẩm thành công");
                } else {
                    request.setAttribute("error", "Thao tác xoá sản phẩm thất bại");
                }
                //Chuyển tiếp người dùng về action=LIST để liệt kê lại danh sách sản phảm
                request.getRequestDispatcher("ManageProduct?action=LIST").forward(request, response);
                break;
            default:
                throw new AssertionError();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
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
     *
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
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
