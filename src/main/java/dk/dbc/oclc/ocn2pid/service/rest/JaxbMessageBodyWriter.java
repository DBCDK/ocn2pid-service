package dk.dbc.oclc.ocn2pid.service.rest;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;

@Provider
@Produces({ MediaType.APPLICATION_XML })
public class JaxbMessageBodyWriter implements MessageBodyWriter<Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JaxbMessageBodyWriter.class);
    private static final ConcurrentHashMap<String, JAXBContext> CONTEXTS = new ConcurrentHashMap<>();

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    @Override
    public long getSize(Object les, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(Object les, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws WebApplicationException {

        try {
            if (type == null) {
                LOGGER.error("type is null");
                return;
            }
            if (mediaType != null && !mediaType.equals(MediaType.APPLICATION_XML_TYPE)) {
                LOGGER.error("mediaType is not {}", MediaType.APPLICATION_XML);
                return;
            }

            final String typeKey = type.getCanonicalName();
            CONTEXTS.computeIfAbsent(typeKey, key -> createJAXBContext(type));
            CONTEXTS.get(typeKey).createMarshaller().marshal(les, entityStream);
        } catch (JAXBException | IllegalStateException e) {
            LOGGER.error("unable to marshall object", e);
        }
    }

    private JAXBContext createJAXBContext(Class<?> type) {
        try {
            return JAXBContext.newInstance(type);
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }
}
