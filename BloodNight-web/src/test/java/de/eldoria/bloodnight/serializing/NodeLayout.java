package de.eldoria.bloodnight.serializing;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.eldoria.bloodnight.bloodmob.serialization.ClassDefinition;
import de.eldoria.bloodnight.bloodmob.nodeimpl.action.OtherPotion;
import org.junit.jupiter.api.Test;

public class NodeLayout {

    @Test
    public void test() throws JsonProcessingException {
        ClassDefinition of = ClassDefinition.of(OtherPotion.class);

        ObjectMapper mapper = new ObjectMapper()
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .setDefaultPrettyPrinter(new DefaultPrettyPrinter().withObjectIndenter(new DefaultIndenter()));
        String s = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(of);
        System.out.println(s);
    }
}
