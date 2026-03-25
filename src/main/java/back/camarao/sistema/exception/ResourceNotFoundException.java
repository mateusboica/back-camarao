package back.camarao.sistema.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String recurso, String id) {
        super("%s não encontrado(a) com id: %s".formatted(recurso, id));
    }
}
