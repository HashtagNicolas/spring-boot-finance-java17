package com.hashtag.ngo.example.bank.archunit;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Vérifie par le code les règles d'architecture du projet :
 * <ol>
 *     <li>l'architecture en couches api -> bean -> entity, sans aucune
 *     dépendance remontante (une couche basse ne doit jamais dépendre d'une
 *     couche plus haute) ;</li>
 *     <li>les conventions de nommage : les contrôleurs REST se terminent
 *     par "Controller", les interfaces de service de la couche "bean" par
 *     "Service", et leurs implémentations par "ServiceImpl".</li>
 * </ol>
 * Ces règles sont exécutées comme des tests JUnit 5 grâce à
 * {@code archunit-junit5} : {@code @AnalyzeClasses} indique le package à
 * scanner, chaque champ {@code @ArchTest} est une règle vérifiée
 * indépendamment.
 */
@AnalyzeClasses(
        packages = "com.hashtag.ngo.example.bank",
        importOptions = ImportOption.DoNotIncludeTests.class
)
public class ArchitectureTest {

    @ArchTest
    static final ArchRule les_couches_ne_dependent_pas_les_unes_des_autres_dans_le_mauvais_sens =
            layeredArchitecture()
                    .consideringAllDependencies()
                    .layer("Api").definedBy("com.hashtag.ngo.example.bank.api..")
                    .layer("Bean").definedBy("com.hashtag.ngo.example.bank.bean..")
                    .layer("Entity").definedBy("com.hashtag.ngo.example.bank.entity..")
                    .whereLayer("Api").mayNotBeAccessedByAnyLayer()
                    .whereLayer("Bean").mayOnlyBeAccessedByLayers("Api")
                    .whereLayer("Entity").mayOnlyBeAccessedByLayers("Api", "Bean");

    @ArchTest
    static final ArchRule les_controllers_se_terminent_par_controller =
            classes().that().areAnnotatedWith(RestController.class)
                    .should().haveSimpleNameEndingWith("Controller");

    @ArchTest
    static final ArchRule les_interfaces_de_la_couche_bean_se_terminent_par_service =
            classes().that().resideInAPackage("com.hashtag.ngo.example.bank.bean..")
                    .and().areInterfaces()
                    .should().haveSimpleNameEndingWith("Service");

    @ArchTest
    static final ArchRule les_implementations_de_la_couche_bean_se_terminent_par_serviceimpl =
            classes().that().resideInAPackage("com.hashtag.ngo.example.bank.bean..")
                    .and().areNotInterfaces()
                    // Exclut les classes synthétiques générées par le compilateur (ex.
                    // "AccountServiceImpl$1", la table de correspondance générée pour un
                    // "switch" sur une enum) : ce ne sont pas des classes du code source.
                    .and().haveNameNotMatching(".*\\$\\d+$")
                    .should().haveSimpleNameEndingWith("ServiceImpl");
}
