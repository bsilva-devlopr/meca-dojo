package br.com.mecadojo.api;

import br.com.mecadojo.api.support.AbstractPostgreSqlTestcontainer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MecaDojoApiApplicationTests extends AbstractPostgreSqlTestcontainer {

    @Test
    void contextLoads() {
    }
}
