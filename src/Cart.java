

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

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
 * Servlet implementation class Cart
 */
@WebServlet("/Cart")
public class Cart extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static String User = null;
    private static String Pass = null;
    private static String Page = "/FabFlix/Cart";
	private DataSource dataSource;
    private Connection connection;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Cart() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException 
	{
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
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		HttpSession session = request.getSession();
	    synchronized(session) 
	    {
	         User = (String) session.getAttribute("User");
	         Pass = (String) session.getAttribute("Pass");
	         session.setAttribute("Page", Page);
        }
        if (User.isEmpty() || Pass.isEmpty())
       	 response.sendRedirect("/FabFlix/index.html");
	    try {
			print(response, request);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void print(HttpServletResponse response, HttpServletRequest request) throws SQLException, IOException
	{
		connection = (Connection) dataSource.getConnection();
		PrintWriter out = response.getWriter();
		String custid = "Select id from customers where first_name = '" + User + "' and password = '" + Pass + "';";
		PreparedStatement ps_custid = (PreparedStatement) connection.prepareStatement(custid);
		ResultSet id = ps_custid.executeQuery();
		id.next();
		int customer_id = Integer.parseInt(id.getString("id"));
		
		int movie_id = Integer.parseInt(request.getParameter("MovieID"));
		if (movie_id != 0)
		{
			String movie_check = "Select * from cart where movie_id = '" + movie_id + "' and customer_id = '" + customer_id + "';";
			PreparedStatement ps_cart_check = (PreparedStatement) connection.prepareStatement(movie_check);
			ResultSet cart_check = ps_cart_check.executeQuery();
			
			String req = request.getParameter("req");
			if (cart_check.next() && !req.equals("del"))
			{
				out.println("<HTML>In cart_check.next()");
				int qty = Integer.parseInt(request.getParameter("qty"));
				qty += Integer.parseInt(cart_check.getString("quantity"));
				String update = "update `moviedb`.`cart` set `quantity` = '" + qty + "' where "
						+ "`movie_id` =  '" + movie_id + "' and `customer_id` = '" + customer_id + "';";
				PreparedStatement ps_cart_update = (PreparedStatement) connection.prepareStatement(update);
				ps_cart_update.executeUpdate();
			}
			else
			{
				if (req.equals("add"))
				{
					out.println("<HTML>In rem_flag = false");
					String movie = "Select * from movies where id = '" + movie_id + "';";
					PreparedStatement ps_movie = (PreparedStatement) connection.prepareStatement(movie);
					ResultSet movies = ps_movie.executeQuery();
					movies.next();
					String insert = "INSERT INTO cart (`title`, `price`, `quantity`, `customer_id`, `movie_id`) "
							+ "VALUES ('" + movies.getString("title") +  "(" + movies.getString("year") + ")', '12.35', '1', '" + customer_id + "', '" + movie_id + "');";
					PreparedStatement ps_cart_insert = (PreparedStatement) connection.prepareStatement(insert);
					ps_cart_insert.executeUpdate();
				}
				else if (req.equals("del"))
				{
					out.println("<HTML>In rem_flag = true");
					String remove = "DELETE from `moviedb`.`cart` where `customer_id` = '" + customer_id + "' and `movie_id` = '" + movie_id + "';";
					PreparedStatement ps_cart_remove = (PreparedStatement) connection.prepareStatement(remove);
					ps_cart_remove.executeUpdate();
				}
			}
		}
		
		String query = "Select * from cart where customer_id like '" + customer_id + "'";
		PreparedStatement ps_cart = (PreparedStatement) connection.prepareStatement(query);
		ResultSet cart = ps_cart.executeQuery();
		
		out.println("<HEAD><TITLE>login</TITLE></HEAD>");
		out.println("<BODY><H1 ALIGN=\"CENTER\">Shopping Cart</H1></CENTER>");
		out.println("<BODY><H4 ALIGN=\"CENTER\">" + User + " " + Pass + "</H4></CENTER>");
		out.println("<BODY><H4 ALIGN=\"CENTER\">" + request.getParameter("MovieID") + "</H4></CENTER>");
		out.println("<BODY><H4 ALIGN=\"CENTER\">" + request.getParameter("req") + "</H4></CENTER>");
		out.println("<BODY><H4 ALIGN=\"CENTER\">" + request.getParameter("qty") + "</H4></CENTER>");
		out.println("<table border>"
				+ "<tr><th>Movie Title</th>"
				+ "<th>Price</th>"
				+ "<th>Quantity</th>"
				+ "<th>Update</th>"
				+ "<th>Remove</th></tr>");
		
		int iter_form = 0;
		while(cart.next())
		{
			out.println("<tr><td>" + cart.getString("title") + "</td>");
			out.println("<td>" + cart.getString("price") + "</td>");
			out.println("<form id=\"form" + iter_form  + "\"><td><input type=\"text\" name=\"quantity\" value=" + cart.getString("quantity") + "></form></td>");
			out.println("<td><button onclick=\"operation('add')\">Update</button></td>");
			out.println("<td><button onclick=\"operation('del')\">Delete</button></td></tr>");
			out.println("<script> function operation(opt) {"
					+ "var x = document.getElementById(\"form" + iter_form  + "\");"
					+ "var quant = x.elements[0].value;"
					+ "var url = \"/FabFlix/Cart?MovieID=" + cart.getString("movie_id") + "&qty=\";"
					+ "url += quant;"
					+ "url += \"&req=\";"
					+ "url += opt;"
					+ "window.location.href=url;}"
					+ "</script>");
			iter_form++;
		}
		out.println("</table>");
		
		 

		out.println("</BODY></HTML>");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
