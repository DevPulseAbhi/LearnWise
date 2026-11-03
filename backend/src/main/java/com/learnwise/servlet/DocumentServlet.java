dfkdjfjjjjjjfkejejjkjfkjdbnpackage com.learnwise.servlet;

import com.learnwise.services.AuthService;
import com.learnwise.services.DocumentService;
import com.learnwise.dto.ErrorResponse;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.Instant;

@MultipartConfig(maxFileSize = 10 * 1024 * 1024, maxRequestSize = 10 * 1024 * 1024)
public final class DocumentServlet extends HttpServlet {
    private DocumentService documents;
    @Override public void init() { documents = (DocumentService) getServletContext().getAttribute(DocumentService.class.getName()); }
    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var current = current(request);
        JsonSupport.write(response, 200, documents.list(current.user().getId()));
    }
    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        var current = current(request); var path = request.getPathInfo() == null ? "" : request.getPathInfo();
        try {
            if (path.isEmpty() || "/".equals(path)) {
                var part = request.getPart("file");
                if (part == null) throw new IllegalArgumentException("A PDF file is required");
                var view = documents.upload(current.user().getId(), part.getSubmittedFileName(), part.getContentType(), part.getInputStream().readAllBytes());
                JsonSupport.write(response, 201, view); return;
            }
            if (path.matches("/\\d+/retry")) {
                var id = Long.parseLong(path.substring(1, path.indexOf("/retry")));
                JsonSupport.write(response, 200, documents.retry(current.user().getId(), id)); return;
            }
            JsonSupport.write(response, 404, error("NOT_FOUND", "Document route not found", request));
        } catch (IllegalArgumentException exception) { JsonSupport.write(response, 400, error("INVALID_DOCUMENT", exception.getMessage(), request)); }
        catch (IllegalStateException exception) { JsonSupport.write(response, 409, error("DOCUMENT_STATE", exception.getMessage(), request)); }
    }
    @Override protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var current = current(request); var path = request.getPathInfo() == null ? "" : request.getPathInfo();
        try { if (!path.matches("/\\d+")) throw new IllegalArgumentException("Document id is required"); documents.delete(current.user().getId(), Long.parseLong(path.substring(1))); response.setStatus(204); }
        catch (IllegalArgumentException exception) { JsonSupport.write(response, 404, error("DOCUMENT_NOT_FOUND", "Document not found", request)); }
    }
    private static AuthService.AuthenticatedSession current(HttpServletRequest r) { return (AuthService.AuthenticatedSession) r.getAttribute(AuthFilter.AUTHENTICATED); }
    private static ErrorResponse error(String code, String message, HttpServletRequest r) { return new ErrorResponse(code, message, Instant.now(), String.valueOf(r.getAttribute("requestId"))); }
}
