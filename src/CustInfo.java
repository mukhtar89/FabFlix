

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

/**
 * Servlet implementation class CustInfo
 */
@WebServlet("/CustInfo")
public class CustInfo extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private DataSource dataSource;
    private Connection connection;
    private static String User = null;
    private static String Pass = null;
    private static String Page = "/Fabflix/CustInfo";
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CustInfo() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		try {
            // Get DataSource
            Context initContext  = new InitialContext();
            dataSource = (DataSource) initContext.lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
	}

	/**
	 * @see Servlet#getServletConfig()
	 */
	public ServletConfig getServletConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see Servlet#getServletInfo()
	 */
	public String getServletInfo() {
		// TODO Auto-generated method stub
		return null; 
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		try {
			connection = (Connection) dataSource.getConnection();
			Statement statement = connection.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSession session = request.getSession();
	    synchronized(session) 
	    {
	         User = (String) session.getAttribute("User");
	         Pass = (String) session.getAttribute("Pass");
	         session.setAttribute("Page", Page);
        }
        if (User.isEmpty() || Pass.isEmpty())
       	 response.sendRedirect("/Fabflix/index.html");
        
	    try {
			print(response, request);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
		
        
	public void print(HttpServletResponse response, HttpServletRequest request) throws SQLException, IOException
    {
		PrintWriter out = response.getWriter();
		String custid = "Select id from customers where first_name = '" + User + "' and password = '" + Pass + "';";
		PreparedStatement ps_custid = (PreparedStatement) connection.prepareStatement(custid);
		ResultSet id = ps_custid.executeQuery();
		id.next();
		int customer_id = Integer.parseInt(id.getString("id"));
		
		String custinfo = "Select * from customers where id = '" + customer_id + "';";
		PreparedStatement ps_custinfo = (PreparedStatement) connection.prepareStatement(custinfo);
		ResultSet customer = ps_custinfo.executeQuery();
		
		out.println("<HTML><style>"
				+ "#container {"
				+ "padding:10%"
		 		+ "height:250px;"
				+ "margin:20%;}"
		 		+ "#details {"
		 		+ "text-align:center;"
		 		+ "padding:5px;"
		 		+ "width:70%;"
		 		+ "color:white;"
		 		+ "background-color:black;"
		 		+ "height:220px;"
		 		+ "float:left;"
		 		+ "}"
		 		+ "</style>");
		out.println("<HEAD><TITLE>Customer Info</TITLE></HEAD>");
		out.println("<BODY><H1 ALIGN=\"CENTER\">Customer Details</H1></CENTER>");
		
		if(customer.next())
		{
			out.println("<div id=\"container\"><div id=\"details\">");
			out.println("<form action=\"/Fabflix/Confirm\" method='post'><span class=\"title\">First Name = </span>");
			out.println("<input type='text' name='fname' value=" + customer.getString("first_name") + "><br><br>");
			out.println("<span class=\"title\">Last Name = </span>");
			out.println("<input type='text' name='lname' value=" + customer.getString("last_name") + "><br><br>");
			out.println("<span class=\"title\">Address Name = </span>");
			out.println("<input type='text' name='add' value='" + customer.getString("address") + "'><br><br>");
			out.println("<span class=\"title\">Email = </span>");
			out.println("<input type='text' name='email' value=" + customer.getString("email") + "><br><br>");
			out.println("<span class=\"title\">Credit Card = </span>");
			out.println("<input type='text' name='ccid' value='" + customer.getString("cc_id") + "'><br><br>");
			out.println("<input type='hidden' name='password' value='" + customer.getString("password") + "'>");
			out.println("<input type=\"submit\" value='Confirm Details' style=\"padding:10px;background-color:blue;color:white;\"></form></div></div>");
		}
		out.println("</BODY></HTML>");
	    	
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
