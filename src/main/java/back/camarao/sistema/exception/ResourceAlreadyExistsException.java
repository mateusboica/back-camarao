package back.camarao.sistema.exception;

public class ResourceAlreadyExistsException extends RuntimeException {
    public ResourceAlreadyExistsException(String mensagem) {
        super(mensagem);
    }
}
