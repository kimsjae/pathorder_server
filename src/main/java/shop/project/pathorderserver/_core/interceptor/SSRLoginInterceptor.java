package shop.project.pathorderserver._core.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;
import shop.project.pathorderserver._core.errors.exceptionssr.Exception401;
import shop.project.pathorderserver.store.SessionStore;

public class SSRLoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();

        SessionStore sessionStore = (SessionStore) session.getAttribute("sessionStore");
        if (sessionStore == null) {
            throw new Exception401("로그인 하셔야 해요");
        }
        return true;
    }
}