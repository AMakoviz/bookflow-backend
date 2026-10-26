package com.bookflow.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Service;

@AnalyzeClasses(packages = "com.bookflow", importOptions = ImportOption.DoNotIncludeTests.class)
class LayerArchitectureTest {

  @ArchTest
  static final ArchRule web_layers_do_not_access_persistence =
      noClasses()
          .that()
          .resideInAnyPackage("..controller..", "..handler..", "..filter..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..repository..", "..entity..")
          .because("HTTP layers do not access persistence directly");

  @ArchTest
  static final ArchRule controllers_and_handlers_return_dtos_without_response_entity =
      noClasses()
          .that()
          .resideInAnyPackage("..controller..", "..handler..")
          .should()
          .dependOnClassesThat()
          .haveFullyQualifiedName("org.springframework.http.ResponseEntity")
          .because("Spring MVC and Jackson serialize directly returned response DTOs");

  @ArchTest
  static final ArchRule controllers_depend_on_service_contracts_not_implementations =
      noClasses()
          .that()
          .resideInAPackage("..controller..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..service.impl..")
          .because("controllers depend on service interfaces");

  @ArchTest
  static final ArchRule service_implementations_follow_enterprise_convention =
      classes()
          .that()
          .resideInAPackage("..service.impl..")
          .should()
          .haveSimpleNameEndingWith("ServiceImpl")
          .andShould()
          .beAnnotatedWith(Service.class);

  @ArchTest
  static final ArchRule service_contract_package_contains_interfaces =
      classes().that().resideInAPackage("com.bookflow.service").should().beInterfaces();

  @ArchTest
  static final ArchRule mapstruct_mappers_are_interfaces =
      classes()
          .that()
          .resideInAPackage("..mapper..")
          .and()
          .haveSimpleNameEndingWith("Mapper")
          .should()
          .beInterfaces();

  @ArchTest
  static final ArchRule dtos_are_independent_of_persistence_and_services =
      noClasses()
          .that()
          .resideInAPackage("..dto..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "..entity..", "..repository..", "..service..", "..controller..", "..mapper..")
          .because("DTOs are transport models without orchestration or persistence dependencies");

  @ArchTest
  static final ArchRule repositories_do_not_depend_on_upper_layers =
      noClasses()
          .that()
          .resideInAPackage("..repository..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..controller..", "..dto..", "..service..", "..mapper..")
          .because("repositories contain persistence access only");

  @ArchTest
  static final ArchRule validation_is_independent_of_web_and_persistence =
      noClasses()
          .that()
          .resideInAPackage("..validation..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..controller..", "..dto..", "..repository..", "..entity..")
          .because("validation rules remain independent of HTTP and persistence");
}
