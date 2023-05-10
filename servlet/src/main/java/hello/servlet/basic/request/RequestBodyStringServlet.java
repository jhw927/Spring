package hello.servlet.basic.request;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@WebServlet(name = "requestBodyStringServlet", urlPatterns = "/request-body-string")
public class RequestBodyStringServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletInputStream inputStream = request.getInputStream(); // 메시지 바디의 내용을 바이트 코드로 얻을 수 있음
        System.out.println("inputStream = " + inputStream);
        String  messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8); // 바이트 코드를 문자로 변환

        System.out.println("messageBody = " + messageBody);
        response.getWriter().write("ok");
    }
}
