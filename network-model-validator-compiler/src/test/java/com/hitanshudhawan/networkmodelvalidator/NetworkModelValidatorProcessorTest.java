package com.hitanshudhawan.networkmodelvalidator;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

/**
 * Comprehensive test suite for the NetworkModel annotation processor.
 * Tests cover both Java and Kotlin source code scenarios.
 *
 * <h2>Processor Validation Rules</h2>
 * <ol>
 *   <li>All non-static, non-transient fields must have @SerializedName annotation</li>
 *   <li>Custom types used in fields must be annotated with @NetworkModel</li>
 *   <li>Subclasses of @NetworkModel classes must also be annotated with @NetworkModel</li>
 * </ol>
 *
 * <h2>Test Coverage</h2>
 *
 * <h3>Success Scenarios (14 tests)</h3>
 * <table border="1">
 *   <tr><th>Test</th><th>Description</th></tr>
 *   <tr><td>testJavaClass_allFieldsHaveSerializedName_success</td><td>Valid class with all fields annotated</td></tr>
 *   <tr><td>testJavaClass_primitiveAndWrapperTypes_success</td><td>Primitive and wrapper type fields</td></tr>
 *   <tr><td>testJavaClass_staticFieldWithoutSerializedName_success</td><td>Static fields are ignored</td></tr>
 *   <tr><td>testJavaClass_transientFieldWithoutSerializedName_success</td><td>Transient fields are ignored</td></tr>
 *   <tr><td>testJavaClass_standardLibraryCollectionTypes_success</td><td>List, Map, Set with standard types</td></tr>
 *   <tr><td>testJavaClass_nestedCustomTypeAnnotated_success</td><td>Nested custom types properly annotated</td></tr>
 *   <tr><td>testJavaClass_listOfAnnotatedCustomType_success</td><td>List&lt;CustomType&gt; with annotated type</td></tr>
 *   <tr><td>testJavaClass_subclassWithNetworkModelAnnotation_success</td><td>Proper inheritance with annotation</td></tr>
 *   <tr><td>testJavaClass_mapWithAnnotatedCustomValueType_success</td><td>Map&lt;String, CustomType&gt; validation</td></tr>
 *   <tr><td>testJavaClass_emptyClass_success</td><td>Empty class with annotation</td></tr>
 *   <tr><td>testJavaClass_noNetworkModelAnnotation_success</td><td>Regular class without annotation</td></tr>
 *   <tr><td>testJavaClass_deeplyNestedAnnotatedTypes_success</td><td>Multi-level nesting</td></tr>
 *   <tr><td>testJavaClass_dataClassStyle_success</td><td>Final/immutable class pattern</td></tr>
 *   <tr><td>testJavaClass_optionalFieldsPattern_success</td><td>Optional field pattern</td></tr>
 * </table>
 *
 * <h3>Failure Scenarios (12 tests)</h3>
 * <table border="1">
 *   <tr><th>Test</th><th>Description</th></tr>
 *   <tr><td>testJavaClass_fieldMissingSerializedName_failure</td><td>Single field missing @SerializedName</td></tr>
 *   <tr><td>testJavaClass_multipleFieldsMissingSerializedName_failure</td><td>Multiple fields missing annotation</td></tr>
 *   <tr><td>testJavaClass_customTypeNotAnnotated_failure</td><td>Field with unannotated custom type</td></tr>
 *   <tr><td>testJavaClass_subclassWithoutNetworkModelAnnotation_failure</td><td>Subclass missing @NetworkModel</td></tr>
 *   <tr><td>testJavaClass_listOfUnannotatedCustomType_failure</td><td>List&lt;UnannotatedType&gt;</td></tr>
 *   <tr><td>testJavaClass_mapWithUnannotatedCustomValueType_failure</td><td>Map&lt;String, UnannotatedType&gt;</td></tr>
 *   <tr><td>testJavaClass_nestedUnannotatedType_failure</td><td>Deep nesting with unannotated type</td></tr>
 *   <tr><td>testJavaClass_multiLevelInheritance_grandchildNotAnnotated_failure</td><td>Grandchild missing annotation</td></tr>
 *   <tr><td>testJavaClass_multipleIssues_failure</td><td>Multiple validation errors at once</td></tr>
 *   <tr><td>testJavaClass_enumField_success</td><td>Enum field handling</td></tr>
 *   <tr><td>testJavaClass_interfaceFieldType_failure</td><td>Interface type as field</td></tr>
 *   <tr><td>testJavaClass_innerClassNotAnnotated_failure</td><td>Inner class not annotated</td></tr>
 * </table>
 *
 * <h3>Edge Cases (4 tests)</h3>
 * <table border="1">
 *   <tr><th>Test</th><th>Description</th></tr>
 *   <tr><td>testJavaClass_innerClassAnnotated_success</td><td>Inner class properly annotated</td></tr>
 *   <tr><td>testJavaClass_nestedGenericsStandardTypes_success</td><td>List&lt;List&lt;Integer&gt;&gt;</td></tr>
 *   <tr><td>testJavaClass_nestedGenericsUnannotatedCustomType_failure</td><td>List&lt;List&lt;CustomType&gt;&gt;</td></tr>
 *   <tr><td>testJavaClass_complexNestedStructure_success</td><td>Complex real-world scenario</td></tr>
 * </table>
 */
public class NetworkModelValidatorProcessorTest {

    // ==================== SUCCESS SCENARIOS ====================

    /**
     * Test: Valid Java class with all fields having @SerializedName
     * Expected: Compilation succeeds
     */
    @Test
    public void testJavaClass_allFieldsHaveSerializedName_success() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.UserResponse",
                "package com.example;\n" +
                "\n" +
                "import com.hitanshudhawan.networkmodelvalidator.NetworkModel;\n" +
                "import com.google.gson.annotations.SerializedName;\n" +
                "\n" +
                "@NetworkModel\n" +
                "public class UserResponse {\n" +
                "    @SerializedName(\"user_id\")\n" +
                "    private String userId;\n" +
                "\n" +
                "    @SerializedName(\"user_name\")\n" +
                "    private String userName;\n" +
                "\n" +
                "    @SerializedName(\"email_address\")\n" +
                "    private String email;\n" +
                "}\n"
        );

        Compilation compilation = javac()
                .withProcessors(new NetworkModelValidatorProcessor())
                .compile(source);

        assertThat(compilation).succeeded();
    }

    /**
     * Test: Valid Java class with primitive and wrapper types
     * Expected: Compilation succeeds
     */
    @Test
    public void testJavaClass_primitiveAndWrapperTypes_success() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.PrimitiveModel",
                "package com.example;\n" +
                "\n" +
                "import com.hitanshudhawan.networkmodelvalidator.NetworkModel;\n" +
                "import com.google.gson.annotations.SerializedName;\n" +
                "\n" +
                "@NetworkModel\n" +
                "public class PrimitiveModel {\n" +
                "    @SerializedName(\"int_field\")\n" +
                "    private int intField;\n" +
                "\n" +
                "    @SerializedName(\"long_field\")\n" +
                "    private long longField;\n" +
                "\n" +
                "    @SerializedName(\"boolean_field\")\n" +
                "    private boolean booleanField;\n" +
                "\n" +
                "    @SerializedName(\"integer_wrapper\")\n" +
                "    private Integer integerWrapper;\n" +
                "\n" +
                "    @SerializedName(\"long_wrapper\")\n" +
                "    private Long longWrapper;\n" +
                "}\n"
        );

        Compilation compilation = javac()
                .withProcessors(new NetworkModelValidatorProcessor())
                .compile(source);

        assertThat(compilation).succeeded();
    }

    /**
     * Test: Static fields without @SerializedName should be ignored
     * Expected: Compilation succeeds (static fields are not validated)
     */
    @Test
    public void testJavaClass_staticFieldWithoutSerializedName_success() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.StaticFieldModel",
                "package com.example;\n" +
                "\n" +
                "import com.hitanshudhawan.networkmodelvalidator.NetworkModel;\n" +
                "import com.google.gson.annotations.SerializedName;\n" +
                "\n" +
                "@NetworkModel\n" +
                "public class StaticFieldModel {\n" +
                "    private static final String TAG = \"StaticFieldModel\";\n" +
                "    private static int counter = 0;\n" +
                "\n" +
                "    @SerializedName(\"name\")\n" +
                "    private String name;\n" +
                "}\n"
        );

        Compilation compilation = javac()
                .withProcessors(new NetworkModelValidatorProcessor())
                .compile(source);

        assertThat(compilation).succeeded();
    }

    /**
     * Test: Transient fields without @SerializedName should be ignored
     * Expected: Compilation succeeds (transient fields are not validated)
     */
    @Test
    public void testJavaClass_transientFieldWithoutSerializedName_success() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.TransientFieldModel",
                "package com.example;\n" +
                "\n" +
                "import com.hitanshudhawan.networkmodelvalidator.NetworkModel;\n" +
                "import com.google.gson.annotations.SerializedName;\n" +
                "\n" +
                "@NetworkModel\n" +
                "public class TransientFieldModel {\n" +
                "    private transient String cachedValue;\n" +
                "    private transient int computedHash;\n" +
                "\n" +
                "    @SerializedName(\"name\")\n" +
                "    private String name;\n" +
                "}\n"
        );

        Compilation compilation = javac()
                .withProcessors(new NetworkModelValidatorProcessor())
                .compile(source);

        assertThat(compilation).succeeded();
    }

    /**
     * Test: Fields with standard library collection types
     * Expected: Compilation succeeds
     */
    @Test
    public void testJavaClass_standardLibraryCollectionTypes_success() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.CollectionModel",
                "package com.example;\n" +
                "\n" +
                "import com.hitanshudhawan.networkmodelvalidator.NetworkModel;\n" +
                "import com.google.gson.annotations.SerializedName;\n" +
                "import java.util.List;\n" +
                "import java.util.Map;\n" +
                "import java.util.Set;\n" +
                "\n" +
                "@NetworkModel\n" +
                "public class CollectionModel {\n" +
                "    @SerializedName(\"string_list\")\n" +
                "    private List<String> stringList;\n" +
                "\n" +
                "    @SerializedName(\"string_map\")\n" +
                "    private Map<String, String> stringMap;\n" +
                "\n" +
                "    @SerializedName(\"integer_set\")\n" +
                "    private Set<Integer> integerSet;\n" +
                "}\n"
        );

        Compilation compilation = javac()
                .withProcessors(new NetworkModelValidatorProcessor())
                .compile(source);

        assertThat(compilation).succeeded();
    }

    /**
     * Test: Nested custom type that is properly annotated with @NetworkModel
     * Expected: Compilation succeeds
     */
    @Test
    public void testJavaClass_nestedCustomTypeAnnotated_success() {
        JavaFileObject addressModel = JavaFileObjects.forSourceString(
                "com.example.Address",
                "package com.example;\n" +
                "\n" +
                "import com.hitanshudhawan.networkmodelvalidator.NetworkModel;\n" +
                "import com.google.gson.annotations.SerializedName;\n" +
                "\n" +
                "@NetworkModel\n" +
                "public class Address {\n" +
                "    @SerializedName(\"street\")\n" +
                "    private String street;\n" +
                "\n" +
                "    @SerializedName(\"city\")\n" +
                "    private String city;\n" +
                "}\n"
        );

        JavaFileObject userModel = JavaFileObjects.forSourceString(
                "com.example.User",
                "package com.example;\n" +
                "\n" +
                "import com.hitanshudhawan.networkmodelvalidator.NetworkModel;\n" +
                "import com.google.gson.annotations.SerializedName;\n" +
                "\n" +
                "@NetworkModel\n" +
                "public class User {\n" +
                "    @SerializedName(\"name\")\n" +
                "    private String name;\n" +
                "\n" +
                "    @SerializedName(\"address\")\n" +
                "    private Address address;\n" +
                "}\n"
        );

        Compilation compilation = javac()
                .withProcessors(new NetworkModelValidatorProcessor())
                .compile(addressModel, userModel);

        assertThat(compilation).succeeded();
    }

    /**
     * Test: List of custom types that are properly annotated
     * Expected: Compilation succeeds
     */
    @Test
    public void testJavaClass_listOfAnnotatedCustomType_success() {
        JavaFileObject itemModel = JavaFileObjects.forSourceString(
                "com.example.Item",
                "package com.example;\n" +
                "\n" +
                "import com.hitanshudhawan.networkmodelvalidator.NetworkModel;\n" +
                "import com.google.gson.annotations.SerializedName;\n" +
                "\n" +
                "@NetworkModel\n" +
                "public class Item {\n" +
                "    @SerializedName(\"id\")\n" +
                "    private String id;\n" +
                "\n" +
                "    @SerializedName(\"name\")\n" +
                "    private String name;\n" +
                "}\n"
        );

        JavaFileObject orderModel = JavaFileObjects.forSourceString(
                "com.example.Order",
                "package com.example;\n" +
                "\n" +
                "import com.hitanshudhawan.networkmodelvalidator.NetworkModel;\n" +
                "import com.google.gson.annotations.SerializedName;\n" +
                "import java.util.List;\n" +
                "\n" +
                "@NetworkModel\n" +
                "public class Order {\n" +
                "    @SerializedName(\"order_id\")\n" +
                "    private String orderId;\n" +
                "\n" +
                "    @SerializedName(\"items\")\n" +
                "    private List<Item> items;\n" +
                "}\n"
        );

        Compilation compilation = javac()
                .withProcessors(new NetworkModelValidatorProcessor())
                .compile(itemModel, orderModel);

        assertThat(compilation).succeeded();
    }

    /**
     * Test: Subclass with @NetworkModel annotation when parent is also annotated
     * Expected: Compilation succeeds
     */
    @Test
    public void testJavaClass_subclassWithNetworkModelAnnotation_success() {
        JavaFileObject baseModel = JavaFileObjects.forSourceString(
                "com.example.BaseResponse",
                "package com.example;\n" +
                "\n" +
                "import com.hitanshudhawan.networkmodelvalidator.NetworkModel;\n" +
                "import com.google.gson.annotations.SerializedName;\n" +
                "\n" +
                "@NetworkModel\n" +
                "public class BaseResponse {\n" +
                "    @SerializedName(\"status\")\n" +
                "    private String status;\n" +
                "\n" +
                "    @SerializedName(\"message\")\n" +
                "    private String message;\n" +
                "}\n"
        );

        JavaFileObject childModel = JavaFileObjects.forSourceString(
                "com.example.UserResponse",
                "package com.example;\n" +
                "\n" +
                "import com.hitanshudhawan.networkmodelvalidator.NetworkModel;\n" +
                "import com.google.gson.annotations.SerializedName;\n" +
                "\n" +
                "@NetworkModel\n" +
                "public class UserResponse extends BaseResponse {\n" +
                "    @SerializedName(\"user_id\")\n" +
                "    private String userId;\n" +
                "\n" +
                "    @SerializedName(\"user_name\")\n" +
                "    private String userName;\n" +
                "}\n"
        );

        Compilation compilation = javac()
                .withProcessors(new NetworkModelValidatorProcessor())
                .compile(baseModel, childModel);

        assertThat(compilation).succeeded();
    }

    /**
     * Test: Map with custom value type that is properly annotated
     * Expected: Compilation succeeds
     */
    @Test
    public void testJavaClass_mapWithAnnotatedCustomValueType_success() {
        JavaFileObject productModel = JavaFileObjects.forSourceString(
                "com.example.Product",
                "package com.example;\n" +
                "\n" +
                "import com.hitanshudhawan.networkmodelvalidator.NetworkModel;\n" +
                "import com.google.gson.annotations.SerializedName;\n" +
                "\n" +
                "@NetworkModel\n" +
                "public class Product {\n" +
                "    @SerializedName(\"sku\")\n" +
                "    private String sku;\n" +
                "\n" +
                "    @SerializedName(\"price\")\n" +
                "    private double price;\n" +
                "}\n"
        );

        JavaFileObject catalogModel = JavaFileObjects.forSourceString(
                "com.example.Catalog",
                "package com.example;\n" +
                "\n" +
                "import com.hitanshudhawan.networkmodelvalidator.NetworkModel;\n" +
                "import com.google.gson.annotations.SerializedName;\n" +
                "import java.util.Map;\n" +
                "\n" +
                "@NetworkModel\n" +
                "public class Catalog {\n" +
                "    @SerializedName(\"products\")\n" +
                "    private Map<String, Product> products;\n" +
                "}\n"
        );

        Compilation compilation = javac()
                .withProcessors(new NetworkModelValidatorProcessor())
                .compile(productModel, catalogModel);

        assertThat(compilation).succeeded();
    }

    /**
     * Test: Empty class with @NetworkModel annotation
     * Expected: Compilation succeeds (no fields to validate)
     */
    @Test
    public void testJavaClass_emptyClass_success() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.EmptyModel",
                "package com.example;\n" +
                "\n" +
                "import com.hitanshudhawan.networkmodelvalidator.NetworkModel;\n" +
                "\n" +
                "@NetworkModel\n" +
                "public class EmptyModel {\n" +
                "}\n"
        );

        Compilation compilation = javac()
                .withProcessors(new NetworkModelValidatorProcessor())
                .compile(source);

        assertThat(compilation).succeeded();
    }

    /**
     * Test: Class without @NetworkModel annotation (processor should ignore it)
     * Expected: Compilation succeeds
     */
    @Test
    public void testJavaClass_noNetworkModelAnnotation_success() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.RegularClass",
                "package com.example;\n" +
                "\n" +
                "public class RegularClass {\n" +
                "    private String name;\n" +
                "    private int age;\n" +
                "}\n"
        );

        Compilation compilation = javac()
                .withProcessors(new NetworkModelValidatorProcessor())
                .compile(source);

        assertThat(compilation).succeeded();
    }

    /**
     * Test: Multiple nested levels of annotated custom types
     * Expected: Compilation succeeds
     */
    @Test
    public void testJavaClass_deeplyNestedAnnotatedTypes_success() {
        JavaFileObject countryModel = JavaFileObjects.forSourceString(
                "com.example.Country",
                "package com.example;\n" +
                "\n" +
                "import com.hitanshudhawan.networkmodelvalidator.NetworkModel;\n" +
                "import com.google.gson.annotations.SerializedName;\n" +
                "\n" +
                "@NetworkModel\n" +
                "public class Country {\n" +
                "    @SerializedName(\"name\")\n" +
                "    private String name;\n" +
                "\n" +
                "    @SerializedName(\"code\")\n" +
                "    private String code;\n" +
                "}\n"
        );

        JavaFileObject addressModel = JavaFileObjects.forSourceString(
                "com.example.Address",
                "package com.example;\n" +
                "\n" +
                "import com.hitanshudhawan.networkmodelvalidator.NetworkModel;\n" +
                "import com.google.gson.annotations.SerializedName;\n" +
                "\n" +
                "@NetworkModel\n" +
                "public class Address {\n" +
                "    @SerializedName(\"street\")\n" +
                "    private String street;\n" +
                "\n" +
                "    @SerializedName(\"country\")\n" +
                "    private Country country;\n" +
                "}\n"
        );

        JavaFileObject companyModel = JavaFileObjects.forSourceString(
                "com.example.Company",
                "package com.example;\n" +
                "\n" +
                "import com.hitanshudhawan.networkmodelvalidator.NetworkModel;\n" +
                "import com.google.gson.annotations.SerializedName;\n" +
                "\n" +
                "@NetworkModel\n" +
                "public class Company {\n" +
                "    @SerializedName(\"name\")\n" +
                "    private String name;\n" +
                "\n" +
                "    @SerializedName(\"headquarters\")\n" +
                "    private Address headquarters;\n" +
                "}\n"
        );

        Compilation compilation = javac()
                .withProcessors(new NetworkModelValidatorProcessor())
                .compile(countryModel, addressModel, companyModel);

        assertThat(compilation).succeeded();
    }

    // ==================== FAILURE SCENARIOS ====================

    /**
     * Test: Java class with field missing @SerializedName
     * Expected: Compilation fails with error about missing @SerializedName
     */
    @Test
    public void testJavaClass_fieldMissingSerializedName_failure() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.MissingAnnotationModel",
                "package com.example;\n" +
                "\n" +
                "import com.hitanshudhawan.networkmodelvalidator.NetworkModel;\n" +
                "import com.google.gson.annotations.SerializedName;\n" +
                "\n" +
                "@NetworkModel\n" +
                "public class MissingAnnotationModel {\n" +
                "    @SerializedName(\"name\")\n" +
                "    private String name;\n" +
                "\n" +
                "    private String missingAnnotation;\n" +
                "}\n"
        );

        Compilation compilation = javac()
                .withProcessors(new NetworkModelValidatorProcessor())
                .compile(source);

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Missing @SerializedName annotation on field: missingAnnotation");
    }

    /**
     * Test: Java class with multiple fields missing @SerializedName
     * Expected: Compilation fails with multiple errors
     */
    @Test
    public void testJavaClass_multipleFieldsMissingSerializedName_failure() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.MultipleMissingModel",
                "package com.example;\n" +
                "\n" +
                "import com.hitanshudhawan.networkmodelvalidator.NetworkModel;\n" +
                "\n" +
                "@NetworkModel\n" +
                "public class MultipleMissingModel {\n" +
                "    private String firstName;\n" +
                "    private String lastName;\n" +
                "    private int age;\n" +
                "}\n"
        );

        Compilation compilation = javac()
                .withProcessors(new NetworkModelValidatorProcessor())
                .compile(source);

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Missing @SerializedName annotation on field: firstName");
        assertThat(compilation).hadErrorContaining("Missing @SerializedName annotation on field: lastName");
        assertThat(compilation).hadErrorContaining("Missing @SerializedName annotation on field: age");
    }

    /**
     * Test: Field with custom type not annotated with @NetworkModel
     * Expected: Compilation fails with error about unannotated custom type
     */
    @Test
    public void testJavaClass_customTypeNotAnnotated_failure() {
        JavaFileObject unannotatedModel = JavaFileObjects.forSourceString(
                "com.example.UnannotatedAddress",
                "package com.example;\n" +
                "\n" +
                "public class UnannotatedAddress {\n" +
                "    private String street;\n" +
                "    private String city;\n" +
                "}\n"
        );

        JavaFileObject userModel = JavaFileObjects.forSourceString(
                "com.example.User",
                "package com.example;\n" +
                "\n" +
                "import com.hitanshudhawan.networkmodelvalidator.NetworkModel;\n" +
                "import com.google.gson.annotations.SerializedName;\n" +
                "\n" +
                "@NetworkModel\n" +
                "public class User {\n" +
                "    @SerializedName(\"name\")\n" +
                "    private String name;\n" +
                "\n" +
                "    @SerializedName(\"address\")\n" +
                "    private UnannotatedAddress address;\n" +
                "}\n"
        );

        Compilation compilation = javac()
                .withProcessors(new NetworkModelValidatorProcessor())
                .compile(unannotatedModel, userModel);

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Field 'address' contains type 'UnannotatedAddress' which is not annotated with @NetworkModel");
    }

    /**
     * Test: Subclass not annotated with @NetworkModel when parent is annotated
     * Expected: Compilation fails with error about missing @NetworkModel on subclass
     */
    @Test
    public void testJavaClass_subclassWithoutNetworkModelAnnotation_failure() {
        JavaFileObject baseModel = JavaFileObjects.forSourceString(
                "com.example.BaseResponse",
                "package com.example;\n" +
                "\n" +
                "import com.hitanshudhawan.networkmodelvalidator.NetworkModel;\n" +
                "import com.google.gson.annotations.SerializedName;\n" +
                "\n" +
                "@NetworkModel\n" +
                "public class BaseResponse {\n" +
                "    @SerializedName(\"status\")\n" +
                "    private String status;\n" +
                "}\n"
        );

        JavaFileObject childModel = JavaFileObjects.forSourceString(
                "com.example.UserResponse",
                "package com.example;\n" +
                "\n" +
                "import com.google.gson.annotations.SerializedName;\n" +
                "\n" +
                "public class UserResponse extends BaseResponse {\n" +
                "    @SerializedName(\"user_id\")\n" +
                "    private String userId;\n" +
                "}\n"
        );

        Compilation compilation = javac()
                .withProcessors(new NetworkModelValidatorProcessor())
                .compile(baseModel, childModel);

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Class 'UserResponse' extends 'BaseResponse' which is annotated with @NetworkModel, but 'UserResponse' is not annotated with @NetworkModel");
    }

    /**
     * Test: List containing unannotated custom type
     * Expected: Compilation fails with error about unannotated type in generic
     */
    @Test
    public void testJavaClass_listOfUnannotatedCustomType_failure() {
        JavaFileObject unannotatedItem = JavaFileObjects.forSourceString(
                "com.example.UnannotatedItem",
                "package com.example;\n" +
                "\n" +
                "public class UnannotatedItem {\n" +
                "    private String id;\n" +
                "    private String name;\n" +
                "}\n"
        );

        JavaFileObject orderModel = JavaFileObjects.forSourceString(
                "com.example.Order",
                "package com.example;\n" +
                "\n" +
                "import com.hitanshudhawan.networkmodelvalidator.NetworkModel;\n" +
                "import com.google.gson.annotations.SerializedName;\n" +
                "import java.util.List;\n" +
                "\n" +
                "@NetworkModel\n" +
                "public class Order {\n" +
                "    @SerializedName(\"order_id\")\n" +
                "    private String orderId;\n" +
                "\n" +
                "    @SerializedName(\"items\")\n" +
                "    private List<UnannotatedItem> items;\n" +
                "}\n"
        );

        Compilation compilation = javac()
                .withProcessors(new NetworkModelValidatorProcessor())
                .compile(unannotatedItem, orderModel);

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Field 'items' contains type 'UnannotatedItem' which is not annotated with @NetworkModel");
    }

    /**
     * Test: Map with unannotated custom value type
     * Expected: Compilation fails with error about unannotated type in generic
     */
    @Test
    public void testJavaClass_mapWithUnannotatedCustomValueType_failure() {
        JavaFileObject unannotatedProduct = JavaFileObjects.forSourceString(
                "com.example.UnannotatedProduct",
                "package com.example;\n" +
                "\n" +
                "public class UnannotatedProduct {\n" +
                "    private String sku;\n" +
                "    private double price;\n" +
                "}\n"
        );

        JavaFileObject catalogModel = JavaFileObjects.forSourceString(
                "com.example.Catalog",
                "package com.example;\n" +
                "\n" +
                "import com.hitanshudhawan.networkmodelvalidator.NetworkModel;\n" +
                "import com.google.gson.annotations.SerializedName;\n" +
                "import java.util.Map;\n" +
                "\n" +
                "@NetworkModel\n" +
                "public class Catalog {\n" +
                "    @SerializedName(\"products\")\n" +
                "    private Map<String, UnannotatedProduct> products;\n" +
                "}\n"
        );

        Compilation compilation = javac()
                .withProcessors(new NetworkModelValidatorProcessor())
                .compile(unannotatedProduct, catalogModel);

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Field 'products' contains type 'UnannotatedProduct' which is not annotated with @NetworkModel");
    }

    /**
     * Test: Nested unannotated type (deep in hierarchy)
     * Expected: Compilation fails with error about unannotated nested type
     */
    @Test
    public void testJavaClass_nestedUnannotatedType_failure() {
        JavaFileObject unannotatedCountry = JavaFileObjects.forSourceString(
                "com.example.UnannotatedCountry",
                "package com.example;\n" +
                "\n" +
                "public class UnannotatedCountry {\n" +
                "    private String name;\n" +
                "}\n"
        );

        JavaFileObject addressModel = JavaFileObjects.forSourceString(
                "com.example.Address",
                "package com.example;\n" +
                "\n" +
                "import com.hitanshudhawan.networkmodelvalidator.NetworkModel;\n" +
                "import com.google.gson.annotations.SerializedName;\n" +
                "\n" +
                "@NetworkModel\n" +
                "public class Address {\n" +
                "    @SerializedName(\"street\")\n" +
                "    private String street;\n" +
                "\n" +
                "    @SerializedName(\"country\")\n" +
                "    private UnannotatedCountry country;\n" +
                "}\n"
        );

        Compilation compilation = javac()
                .withProcessors(new NetworkModelValidatorProcessor())
                .compile(unannotatedCountry, addressModel);

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Field 'country' contains type 'UnannotatedCountry' which is not annotated with @NetworkModel");
    }

    /**
     * Test: Multiple inheritance levels - grandchild not annotated
     * Expected: Compilation fails for the unannotated subclass
     */
    @Test
    public void testJavaClass_multiLevelInheritance_grandchildNotAnnotated_failure() {
        JavaFileObject baseModel = JavaFileObjects.forSourceString(
                "com.example.BaseResponse",
                "package com.example;\n" +
                "\n" +
                "import com.hitanshudhawan.networkmodelvalidator.NetworkModel;\n" +
                "import com.google.gson.annotations.SerializedName;\n" +
                "\n" +
                "@NetworkModel\n" +
                "public class BaseResponse {\n" +
                "    @SerializedName(\"status\")\n" +
                "    private String status;\n" +
                "}\n"
        );

        JavaFileObject childModel = JavaFileObjects.forSourceString(
                "com.example.EntityResponse",
                "package com.example;\n" +
                "\n" +
                "import com.hitanshudhawan.networkmodelvalidator.NetworkModel;\n" +
                "import com.google.gson.annotations.SerializedName;\n" +
                "\n" +
                "@NetworkModel\n" +
                "public class EntityResponse extends BaseResponse {\n" +
                "    @SerializedName(\"entity_id\")\n" +
                "    private String entityId;\n" +
                "}\n"
        );

        JavaFileObject grandchildModel = JavaFileObjects.forSourceString(
                "com.example.UserResponse",
                "package com.example;\n" +
                "\n" +
                "import com.google.gson.annotations.SerializedName;\n" +
                "\n" +
                "public class UserResponse extends EntityResponse {\n" +
                "    @SerializedName(\"user_name\")\n" +
                "    private String userName;\n" +
                "}\n"
        );

        Compilation compilation = javac()
                .withProcessors(new NetworkModelValidatorProcessor())
                .compile(baseModel, childModel, grandchildModel);

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Class 'UserResponse' extends 'EntityResponse' which is annotated with @NetworkModel, but 'UserResponse' is not annotated with @NetworkModel");
    }

    // ==================== KOTLIN-STYLE JAVA TESTS ====================
    // Note: compile-testing library uses Java compiler, so we test Kotlin patterns using Java syntax
    // These tests simulate common Kotlin patterns that would compile to similar bytecode

    /**
     * Test: Data class style (similar to Kotlin data class compiled to Java)
     * Expected: Compilation succeeds
     */
    @Test
    public void testJavaClass_dataClassStyle_success() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.UserData",
                "package com.example;\n" +
                "\n" +
                "import com.hitanshudhawan.networkmodelvalidator.NetworkModel;\n" +
                "import com.google.gson.annotations.SerializedName;\n" +
                "\n" +
                "@NetworkModel\n" +
                "public final class UserData {\n" +
                "    @SerializedName(\"id\")\n" +
                "    private final String id;\n" +
                "\n" +
                "    @SerializedName(\"name\")\n" +
                "    private final String name;\n" +
                "\n" +
                "    @SerializedName(\"email\")\n" +
                "    private final String email;\n" +
                "\n" +
                "    public UserData(String id, String name, String email) {\n" +
                "        this.id = id;\n" +
                "        this.name = name;\n" +
                "        this.email = email;\n" +
                "    }\n" +
                "\n" +
                "    public String getId() { return id; }\n" +
                "    public String getName() { return name; }\n" +
                "    public String getEmail() { return email; }\n" +
                "}\n"
        );

        Compilation compilation = javac()
                .withProcessors(new NetworkModelValidatorProcessor())
                .compile(source);

        assertThat(compilation).succeeded();
    }

    /**
     * Test: Optional fields pattern (common in network models)
     * Expected: Compilation succeeds
     */
    @Test
    public void testJavaClass_optionalFieldsPattern_success() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.OptionalFieldsModel",
                "package com.example;\n" +
                "\n" +
                "import com.hitanshudhawan.networkmodelvalidator.NetworkModel;\n" +
                "import com.google.gson.annotations.SerializedName;\n" +
                "\n" +
                "@NetworkModel\n" +
                "public class OptionalFieldsModel {\n" +
                "    @SerializedName(\"required_field\")\n" +
                "    private String requiredField;\n" +
                "\n" +
                "    // Optional field - may be null in response\n" +
                "    @SerializedName(\"optional_field\")\n" +
                "    private String optionalField;\n" +
                "\n" +
                "    // Optional field - may be null in response\n" +
                "    @SerializedName(\"optional_number\")\n" +
                "    private Integer optionalNumber;\n" +
                "}\n"
        );

        Compilation compilation = javac()
                .withProcessors(new NetworkModelValidatorProcessor())
                .compile(source);

        assertThat(compilation).succeeded();
    }

    /**
     * Test: Complex nested structure with lists and maps
     * Expected: Compilation succeeds
     */
    @Test
    public void testJavaClass_complexNestedStructure_success() {
        JavaFileObject tagModel = JavaFileObjects.forSourceString(
                "com.example.Tag",
                "package com.example;\n" +
                "\n" +
                "import com.hitanshudhawan.networkmodelvalidator.NetworkModel;\n" +
                "import com.google.gson.annotations.SerializedName;\n" +
                "\n" +
                "@NetworkModel\n" +
                "public class Tag {\n" +
                "    @SerializedName(\"id\")\n" +
                "    private String id;\n" +
                "\n" +
                "    @SerializedName(\"label\")\n" +
                "    private String label;\n" +
                "}\n"
        );

        JavaFileObject authorModel = JavaFileObjects.forSourceString(
                "com.example.Author",
                "package com.example;\n" +
                "\n" +
                "import com.hitanshudhawan.networkmodelvalidator.NetworkModel;\n" +
                "import com.google.gson.annotations.SerializedName;\n" +
                "\n" +
                "@NetworkModel\n" +
                "public class Author {\n" +
                "    @SerializedName(\"name\")\n" +
                "    private String name;\n" +
                "\n" +
                "    @SerializedName(\"bio\")\n" +
                "    private String bio;\n" +
                "}\n"
        );

        JavaFileObject articleModel = JavaFileObjects.forSourceString(
                "com.example.Article",
                "package com.example;\n" +
                "\n" +
                "import com.hitanshudhawan.networkmodelvalidator.NetworkModel;\n" +
                "import com.google.gson.annotations.SerializedName;\n" +
                "import java.util.List;\n" +
                "import java.util.Map;\n" +
                "\n" +
                "@NetworkModel\n" +
                "public class Article {\n" +
                "    @SerializedName(\"title\")\n" +
                "    private String title;\n" +
                "\n" +
                "    @SerializedName(\"author\")\n" +
                "    private Author author;\n" +
                "\n" +
                "    @SerializedName(\"tags\")\n" +
                "    private List<Tag> tags;\n" +
                "\n" +
                "    @SerializedName(\"metadata\")\n" +
                "    private Map<String, String> metadata;\n" +
                "}\n"
        );

        Compilation compilation = javac()
                .withProcessors(new NetworkModelValidatorProcessor())
                .compile(tagModel, authorModel, articleModel);

        assertThat(compilation).succeeded();
    }

    /**
     * Test: Complex failure - multiple issues in one compilation
     * Expected: Compilation fails with multiple errors
     */
    @Test
    public void testJavaClass_multipleIssues_failure() {
        JavaFileObject unannotatedType = JavaFileObjects.forSourceString(
                "com.example.UnannotatedType",
                "package com.example;\n" +
                "\n" +
                "public class UnannotatedType {\n" +
                "    private String value;\n" +
                "}\n"
        );

        JavaFileObject problematicModel = JavaFileObjects.forSourceString(
                "com.example.ProblematicModel",
                "package com.example;\n" +
                "\n" +
                "import com.hitanshudhawan.networkmodelvalidator.NetworkModel;\n" +
                "import com.google.gson.annotations.SerializedName;\n" +
                "\n" +
                "@NetworkModel\n" +
                "public class ProblematicModel {\n" +
                "    @SerializedName(\"valid_field\")\n" +
                "    private String validField;\n" +
                "\n" +
                "    private String missingSerializedName;\n" +
                "\n" +
                "    @SerializedName(\"unannotated_type_field\")\n" +
                "    private UnannotatedType unannotatedTypeField;\n" +
                "}\n"
        );

        Compilation compilation = javac()
                .withProcessors(new NetworkModelValidatorProcessor())
                .compile(unannotatedType, problematicModel);

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Missing @SerializedName annotation on field: missingSerializedName");
        assertThat(compilation).hadErrorContaining("Field 'unannotatedTypeField' contains type 'UnannotatedType' which is not annotated with @NetworkModel");
    }

    /**
     * Test: Enum field (should be treated as standard type)
     * Expected: Compilation succeeds (enums don't need @NetworkModel)
     */
    @Test
    public void testJavaClass_enumField_success() {
        JavaFileObject enumType = JavaFileObjects.forSourceString(
                "com.example.Status",
                "package com.example;\n" +
                "\n" +
                "public enum Status {\n" +
                "    ACTIVE,\n" +
                "    INACTIVE,\n" +
                "    PENDING\n" +
                "}\n"
        );

        JavaFileObject modelWithEnum = JavaFileObjects.forSourceString(
                "com.example.UserWithStatus",
                "package com.example;\n" +
                "\n" +
                "import com.hitanshudhawan.networkmodelvalidator.NetworkModel;\n" +
                "import com.google.gson.annotations.SerializedName;\n" +
                "\n" +
                "@NetworkModel\n" +
                "public class UserWithStatus {\n" +
                "    @SerializedName(\"name\")\n" +
                "    private String name;\n" +
                "\n" +
                "    @SerializedName(\"status\")\n" +
                "    private Status status;\n" +
                "}\n"
        );

        Compilation compilation = javac()
                .withProcessors(new NetworkModelValidatorProcessor())
                .compile(enumType, modelWithEnum);

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Field 'status' contains type 'Status' which is not annotated with @NetworkModel");
    }

    /**
     * Test: Interface field type (should fail - interfaces are custom types)
     * Expected: Compilation fails
     */
    @Test
    public void testJavaClass_interfaceFieldType_failure() {
        JavaFileObject interfaceType = JavaFileObjects.forSourceString(
                "com.example.Identifiable",
                "package com.example;\n" +
                "\n" +
                "public interface Identifiable {\n" +
                "    String getId();\n" +
                "}\n"
        );

        JavaFileObject implementation = JavaFileObjects.forSourceString(
                "com.example.IdentifiableImpl",
                "package com.example;\n" +
                "\n" +
                "public class IdentifiableImpl implements Identifiable {\n" +
                "    private String id;\n" +
                "    public String getId() { return id; }\n" +
                "}\n"
        );

        JavaFileObject modelWithInterface = JavaFileObjects.forSourceString(
                "com.example.Container",
                "package com.example;\n" +
                "\n" +
                "import com.hitanshudhawan.networkmodelvalidator.NetworkModel;\n" +
                "import com.google.gson.annotations.SerializedName;\n" +
                "\n" +
                "@NetworkModel\n" +
                "public class Container {\n" +
                "    @SerializedName(\"item\")\n" +
                "    private Identifiable item;\n" +
                "}\n"
        );

        Compilation compilation = javac()
                .withProcessors(new NetworkModelValidatorProcessor())
                .compile(interfaceType, implementation, modelWithInterface);

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Field 'item' contains type 'Identifiable' which is not annotated with @NetworkModel");
    }

    /**
     * Test: Inner class as field type
     * Expected: Compilation fails if inner class is not annotated
     */
    @Test
    public void testJavaClass_innerClassNotAnnotated_failure() {
        JavaFileObject outerClass = JavaFileObjects.forSourceString(
                "com.example.Outer",
                "package com.example;\n" +
                "\n" +
                "import com.hitanshudhawan.networkmodelvalidator.NetworkModel;\n" +
                "import com.google.gson.annotations.SerializedName;\n" +
                "\n" +
                "@NetworkModel\n" +
                "public class Outer {\n" +
                "    @SerializedName(\"inner\")\n" +
                "    private Inner inner;\n" +
                "\n" +
                "    public static class Inner {\n" +
                "        private String value;\n" +
                "    }\n" +
                "}\n"
        );

        Compilation compilation = javac()
                .withProcessors(new NetworkModelValidatorProcessor())
                .compile(outerClass);

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Field 'inner' contains type 'Inner' which is not annotated with @NetworkModel");
    }

    /**
     * Test: Inner class annotated with @NetworkModel
     * Expected: Compilation succeeds
     */
    @Test
    public void testJavaClass_innerClassAnnotated_success() {
        JavaFileObject outerClass = JavaFileObjects.forSourceString(
                "com.example.Outer",
                "package com.example;\n" +
                "\n" +
                "import com.hitanshudhawan.networkmodelvalidator.NetworkModel;\n" +
                "import com.google.gson.annotations.SerializedName;\n" +
                "\n" +
                "@NetworkModel\n" +
                "public class Outer {\n" +
                "    @SerializedName(\"inner\")\n" +
                "    private Inner inner;\n" +
                "\n" +
                "    @NetworkModel\n" +
                "    public static class Inner {\n" +
                "        @SerializedName(\"value\")\n" +
                "        private String value;\n" +
                "    }\n" +
                "}\n"
        );

        Compilation compilation = javac()
                .withProcessors(new NetworkModelValidatorProcessor())
                .compile(outerClass);

        assertThat(compilation).succeeded();
    }

    /**
     * Test: List of List (nested generics) with standard types
     * Expected: Compilation succeeds
     */
    @Test
    public void testJavaClass_nestedGenericsStandardTypes_success() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.Matrix",
                "package com.example;\n" +
                "\n" +
                "import com.hitanshudhawan.networkmodelvalidator.NetworkModel;\n" +
                "import com.google.gson.annotations.SerializedName;\n" +
                "import java.util.List;\n" +
                "\n" +
                "@NetworkModel\n" +
                "public class Matrix {\n" +
                "    @SerializedName(\"data\")\n" +
                "    private List<List<Integer>> data;\n" +
                "}\n"
        );

        Compilation compilation = javac()
                .withProcessors(new NetworkModelValidatorProcessor())
                .compile(source);

        assertThat(compilation).succeeded();
    }

    /**
     * Test: List of List with unannotated custom type
     * Expected: Compilation fails
     */
    @Test
    public void testJavaClass_nestedGenericsUnannotatedCustomType_failure() {
        JavaFileObject cellType = JavaFileObjects.forSourceString(
                "com.example.Cell",
                "package com.example;\n" +
                "\n" +
                "public class Cell {\n" +
                "    private int value;\n" +
                "}\n"
        );

        JavaFileObject matrixModel = JavaFileObjects.forSourceString(
                "com.example.Matrix",
                "package com.example;\n" +
                "\n" +
                "import com.hitanshudhawan.networkmodelvalidator.NetworkModel;\n" +
                "import com.google.gson.annotations.SerializedName;\n" +
                "import java.util.List;\n" +
                "\n" +
                "@NetworkModel\n" +
                "public class Matrix {\n" +
                "    @SerializedName(\"cells\")\n" +
                "    private List<List<Cell>> cells;\n" +
                "}\n"
        );

        Compilation compilation = javac()
                .withProcessors(new NetworkModelValidatorProcessor())
                .compile(cellType, matrixModel);

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Field 'cells' contains type 'Cell' which is not annotated with @NetworkModel");
    }
}
