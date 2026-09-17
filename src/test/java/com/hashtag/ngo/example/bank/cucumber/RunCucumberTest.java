package com.hashtag.ngo.example.bank.cucumber;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Point d'entrée JUnit Platform pour l'exécution des scénarios Cucumber.
 * <p>
 * {@code @Suite} + {@code @IncludeEngines("cucumber")} indiquent à Maven
 * Surefire de déléguer l'exécution au moteur fourni par
 * {@code cucumber-junit-platform-engine} ; {@code @SelectClasspathResource}
 * pointe vers le dossier contenant les fichiers {@code .feature}, et le
 * paramètre {@code GLUE_PROPERTY_NAME} indique le package contenant les
 * classes de step definitions et {@link CucumberSpringConfiguration}.
 * <p>
 * Cette classe ne contient aucune logique : "mvn test" l'exécute comme
 * n'importe quelle autre classe de test JUnit.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(
        key = Constants.GLUE_PROPERTY_NAME,
        value = "com.hashtag.ngo.example.bank.cucumber"
)
public class RunCucumberTest {
}
