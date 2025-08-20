package first.webapp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class RegisterServletTest {

    private RegisterServlet registerServlet;
    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;

    @BeforeEach
    void setUp() throws Exception {
        registerServlet = new RegisterServlet();
        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
    }

    @Test
    void testDoPostSuccess() throws Exception {
        // Mock request parameters (must match RegisterServlet.java)
        when(mockRequest.getParameter("userName")).thenReturn("eric");
        when(mockRequest.getParameter("password")).thenReturn("password123");
        when(mockRequest.getParameter("email")).thenReturn("eric@gmail.com");
        when(mockRequest.getParameter("language")).thenReturn("English");

        // Capture response output
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(mockResponse.getWriter()).thenReturn(printWriter);

        // Mock DriverManager static class
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            // Mock connection + prepared statement
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);

            // Call servlet
            registerServlet.doPost(mockRequest, mockResponse);

            // Flush writer
            printWriter.flush();

            // Assert response contains success message
            String responseOutput = stringWriter.toString();
            assertTrue(responseOutput.contains("You are successfully registered"));

            // Verify correct SQL parameters
            verify(mockPreparedStatement).setString(1, "eric");
            verify(mockPreparedStatement).setString(2, "password123");
            verify(mockPreparedStatement).setString(3, "eric@gmail.com");
            verify(mockPreparedStatement).setString(4, "English");

            // Verify executeUpdate was called
            verify(mockPreparedStatement).executeUpdate();
        }
    }
}