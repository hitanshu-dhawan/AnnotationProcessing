package com.hitanshudhawan.networkmodelvalidator.ksp

import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Comprehensive test suite for the KSP NetworkModel processor.
 *
 * This is a direct port of the JSR-269 NetworkModelValidatorProcessorTest. The exact same
 * source strings and expected error messages are used, but the harness is
 * kotlin-compile-testing (kctfork) because com.google.testing.compile (javac) cannot run
 * KSP processors.
 *
 * KSP processes both Kotlin and Java sources, so the Java source strings below exercise the
 * processor for Java inputs, matching the original test suite.
 *
 * Processor Validation Rules:
 * 1. All non-static, non-transient fields must have @SerializedName annotation
 * 2. Custom types used in fields must be annotated with @NetworkModel
 * 3. Subclasses of @NetworkModel classes must also be annotated with @NetworkModel
 */
@OptIn(ExperimentalCompilerApi::class)
class NetworkModelValidatorProcessorTest {

    private fun compile(vararg sources: SourceFile): JvmCompilationResult {
        return KotlinCompilation().apply {
            this.sources = sources.toList()
            // Kotlin 2.2.0 uses the K2 compiler, under which the legacy KSP1 pipeline does not
            // run. KSP2 must be used explicitly. KSP2 processes both Kotlin and Java sources.
            configureKsp(useKsp2 = true) {
                symbolProcessorProviders += NetworkModelValidatorProcessorProvider()
            }
            inheritClassPath = true
            messageOutputStream = System.out
        }.compile()
    }

    private fun assertSucceeded(result: JvmCompilationResult) {
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    private fun assertFailedWith(result: JvmCompilationResult, vararg messages: String) {
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        messages.forEach { message ->
            assertTrue(
                "Expected error message containing: $message\nActual messages:\n${result.messages}",
                result.messages.contains(message)
            )
        }
    }

    // ==================== SUCCESS SCENARIOS ====================

    @Test
    fun testJavaClass_allFieldsHaveSerializedName_success() {
        val source = SourceFile.java(
            "UserResponse.java",
            """
            package com.example;

            import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel;
            import com.google.gson.annotations.SerializedName;

            @NetworkModel
            public class UserResponse {
                @SerializedName("user_id")
                private String userId;

                @SerializedName("user_name")
                private String userName;

                @SerializedName("email_address")
                private String email;
            }
            """.trimIndent()
        )

        assertSucceeded(compile(source))
    }

    @Test
    fun testJavaClass_primitiveAndWrapperTypes_success() {
        val source = SourceFile.java(
            "PrimitiveModel.java",
            """
            package com.example;

            import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel;
            import com.google.gson.annotations.SerializedName;

            @NetworkModel
            public class PrimitiveModel {
                @SerializedName("int_field")
                private int intField;

                @SerializedName("long_field")
                private long longField;

                @SerializedName("boolean_field")
                private boolean booleanField;

                @SerializedName("integer_wrapper")
                private Integer integerWrapper;

                @SerializedName("long_wrapper")
                private Long longWrapper;
            }
            """.trimIndent()
        )

        assertSucceeded(compile(source))
    }

    @Test
    fun testJavaClass_staticFieldWithoutSerializedName_success() {
        val source = SourceFile.java(
            "StaticFieldModel.java",
            """
            package com.example;

            import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel;
            import com.google.gson.annotations.SerializedName;

            @NetworkModel
            public class StaticFieldModel {
                private static final String TAG = "StaticFieldModel";
                private static int counter = 0;

                @SerializedName("name")
                private String name;
            }
            """.trimIndent()
        )

        assertSucceeded(compile(source))
    }

    @Test
    fun testJavaClass_transientFieldWithoutSerializedName_success() {
        val source = SourceFile.java(
            "TransientFieldModel.java",
            """
            package com.example;

            import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel;
            import com.google.gson.annotations.SerializedName;

            @NetworkModel
            public class TransientFieldModel {
                private transient String cachedValue;
                private transient int computedHash;

                @SerializedName("name")
                private String name;
            }
            """.trimIndent()
        )

        assertSucceeded(compile(source))
    }

    @Test
    fun testJavaClass_standardLibraryCollectionTypes_success() {
        val source = SourceFile.java(
            "CollectionModel.java",
            """
            package com.example;

            import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel;
            import com.google.gson.annotations.SerializedName;
            import java.util.List;
            import java.util.Map;
            import java.util.Set;

            @NetworkModel
            public class CollectionModel {
                @SerializedName("string_list")
                private List<String> stringList;

                @SerializedName("string_map")
                private Map<String, String> stringMap;

                @SerializedName("integer_set")
                private Set<Integer> integerSet;
            }
            """.trimIndent()
        )

        assertSucceeded(compile(source))
    }

    @Test
    fun testJavaClass_nestedCustomTypeAnnotated_success() {
        val addressModel = SourceFile.java(
            "Address.java",
            """
            package com.example;

            import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel;
            import com.google.gson.annotations.SerializedName;

            @NetworkModel
            public class Address {
                @SerializedName("street")
                private String street;

                @SerializedName("city")
                private String city;
            }
            """.trimIndent()
        )

        val userModel = SourceFile.java(
            "User.java",
            """
            package com.example;

            import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel;
            import com.google.gson.annotations.SerializedName;

            @NetworkModel
            public class User {
                @SerializedName("name")
                private String name;

                @SerializedName("address")
                private Address address;
            }
            """.trimIndent()
        )

        assertSucceeded(compile(addressModel, userModel))
    }

    @Test
    fun testJavaClass_listOfAnnotatedCustomType_success() {
        val itemModel = SourceFile.java(
            "Item.java",
            """
            package com.example;

            import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel;
            import com.google.gson.annotations.SerializedName;

            @NetworkModel
            public class Item {
                @SerializedName("id")
                private String id;

                @SerializedName("name")
                private String name;
            }
            """.trimIndent()
        )

        val orderModel = SourceFile.java(
            "Order.java",
            """
            package com.example;

            import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel;
            import com.google.gson.annotations.SerializedName;
            import java.util.List;

            @NetworkModel
            public class Order {
                @SerializedName("order_id")
                private String orderId;

                @SerializedName("items")
                private List<Item> items;
            }
            """.trimIndent()
        )

        assertSucceeded(compile(itemModel, orderModel))
    }

    @Test
    fun testJavaClass_subclassWithNetworkModelAnnotation_success() {
        val baseModel = SourceFile.java(
            "BaseResponse.java",
            """
            package com.example;

            import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel;
            import com.google.gson.annotations.SerializedName;

            @NetworkModel
            public class BaseResponse {
                @SerializedName("status")
                private String status;

                @SerializedName("message")
                private String message;
            }
            """.trimIndent()
        )

        val childModel = SourceFile.java(
            "UserResponse.java",
            """
            package com.example;

            import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel;
            import com.google.gson.annotations.SerializedName;

            @NetworkModel
            public class UserResponse extends BaseResponse {
                @SerializedName("user_id")
                private String userId;

                @SerializedName("user_name")
                private String userName;
            }
            """.trimIndent()
        )

        assertSucceeded(compile(baseModel, childModel))
    }

    @Test
    fun testJavaClass_mapWithAnnotatedCustomValueType_success() {
        val productModel = SourceFile.java(
            "Product.java",
            """
            package com.example;

            import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel;
            import com.google.gson.annotations.SerializedName;

            @NetworkModel
            public class Product {
                @SerializedName("sku")
                private String sku;

                @SerializedName("price")
                private double price;
            }
            """.trimIndent()
        )

        val catalogModel = SourceFile.java(
            "Catalog.java",
            """
            package com.example;

            import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel;
            import com.google.gson.annotations.SerializedName;
            import java.util.Map;

            @NetworkModel
            public class Catalog {
                @SerializedName("products")
                private Map<String, Product> products;
            }
            """.trimIndent()
        )

        assertSucceeded(compile(productModel, catalogModel))
    }

    @Test
    fun testJavaClass_emptyClass_success() {
        val source = SourceFile.java(
            "EmptyModel.java",
            """
            package com.example;

            import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel;

            @NetworkModel
            public class EmptyModel {
            }
            """.trimIndent()
        )

        assertSucceeded(compile(source))
    }

    @Test
    fun testJavaClass_noNetworkModelAnnotation_success() {
        val source = SourceFile.java(
            "RegularClass.java",
            """
            package com.example;

            public class RegularClass {
                private String name;
                private int age;
            }
            """.trimIndent()
        )

        assertSucceeded(compile(source))
    }

    @Test
    fun testJavaClass_deeplyNestedAnnotatedTypes_success() {
        val countryModel = SourceFile.java(
            "Country.java",
            """
            package com.example;

            import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel;
            import com.google.gson.annotations.SerializedName;

            @NetworkModel
            public class Country {
                @SerializedName("name")
                private String name;

                @SerializedName("code")
                private String code;
            }
            """.trimIndent()
        )

        val addressModel = SourceFile.java(
            "Address.java",
            """
            package com.example;

            import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel;
            import com.google.gson.annotations.SerializedName;

            @NetworkModel
            public class Address {
                @SerializedName("street")
                private String street;

                @SerializedName("country")
                private Country country;
            }
            """.trimIndent()
        )

        val companyModel = SourceFile.java(
            "Company.java",
            """
            package com.example;

            import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel;
            import com.google.gson.annotations.SerializedName;

            @NetworkModel
            public class Company {
                @SerializedName("name")
                private String name;

                @SerializedName("headquarters")
                private Address headquarters;
            }
            """.trimIndent()
        )

        assertSucceeded(compile(countryModel, addressModel, companyModel))
    }

    @Test
    fun testJavaClass_dataClassStyle_success() {
        val source = SourceFile.java(
            "UserData.java",
            """
            package com.example;

            import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel;
            import com.google.gson.annotations.SerializedName;

            @NetworkModel
            public final class UserData {
                @SerializedName("id")
                private final String id;

                @SerializedName("name")
                private final String name;

                @SerializedName("email")
                private final String email;

                public UserData(String id, String name, String email) {
                    this.id = id;
                    this.name = name;
                    this.email = email;
                }

                public String getId() { return id; }
                public String getName() { return name; }
                public String getEmail() { return email; }
            }
            """.trimIndent()
        )

        assertSucceeded(compile(source))
    }

    @Test
    fun testJavaClass_optionalFieldsPattern_success() {
        val source = SourceFile.java(
            "OptionalFieldsModel.java",
            """
            package com.example;

            import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel;
            import com.google.gson.annotations.SerializedName;

            @NetworkModel
            public class OptionalFieldsModel {
                @SerializedName("required_field")
                private String requiredField;

                // Optional field - may be null in response
                @SerializedName("optional_field")
                private String optionalField;

                // Optional field - may be null in response
                @SerializedName("optional_number")
                private Integer optionalNumber;
            }
            """.trimIndent()
        )

        assertSucceeded(compile(source))
    }

    @Test
    fun testJavaClass_complexNestedStructure_success() {
        val tagModel = SourceFile.java(
            "Tag.java",
            """
            package com.example;

            import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel;
            import com.google.gson.annotations.SerializedName;

            @NetworkModel
            public class Tag {
                @SerializedName("id")
                private String id;

                @SerializedName("label")
                private String label;
            }
            """.trimIndent()
        )

        val authorModel = SourceFile.java(
            "Author.java",
            """
            package com.example;

            import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel;
            import com.google.gson.annotations.SerializedName;

            @NetworkModel
            public class Author {
                @SerializedName("name")
                private String name;

                @SerializedName("bio")
                private String bio;
            }
            """.trimIndent()
        )

        val articleModel = SourceFile.java(
            "Article.java",
            """
            package com.example;

            import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel;
            import com.google.gson.annotations.SerializedName;
            import java.util.List;
            import java.util.Map;

            @NetworkModel
            public class Article {
                @SerializedName("title")
                private String title;

                @SerializedName("author")
                private Author author;

                @SerializedName("tags")
                private List<Tag> tags;

                @SerializedName("metadata")
                private Map<String, String> metadata;
            }
            """.trimIndent()
        )

        assertSucceeded(compile(tagModel, authorModel, articleModel))
    }

    // ==================== FAILURE SCENARIOS ====================

    @Test
    fun testJavaClass_fieldMissingSerializedName_failure() {
        val source = SourceFile.java(
            "MissingAnnotationModel.java",
            """
            package com.example;

            import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel;
            import com.google.gson.annotations.SerializedName;

            @NetworkModel
            public class MissingAnnotationModel {
                @SerializedName("name")
                private String name;

                private String missingAnnotation;
            }
            """.trimIndent()
        )

        assertFailedWith(
            compile(source),
            "Missing @SerializedName annotation on field: missingAnnotation"
        )
    }

    @Test
    fun testJavaClass_multipleFieldsMissingSerializedName_failure() {
        val source = SourceFile.java(
            "MultipleMissingModel.java",
            """
            package com.example;

            import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel;

            @NetworkModel
            public class MultipleMissingModel {
                private String firstName;
                private String lastName;
                private int age;
            }
            """.trimIndent()
        )

        assertFailedWith(
            compile(source),
            "Missing @SerializedName annotation on field: firstName",
            "Missing @SerializedName annotation on field: lastName",
            "Missing @SerializedName annotation on field: age"
        )
    }

    @Test
    fun testJavaClass_customTypeNotAnnotated_failure() {
        val unannotatedModel = SourceFile.java(
            "UnannotatedAddress.java",
            """
            package com.example;

            public class UnannotatedAddress {
                private String street;
                private String city;
            }
            """.trimIndent()
        )

        val userModel = SourceFile.java(
            "User.java",
            """
            package com.example;

            import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel;
            import com.google.gson.annotations.SerializedName;

            @NetworkModel
            public class User {
                @SerializedName("name")
                private String name;

                @SerializedName("address")
                private UnannotatedAddress address;
            }
            """.trimIndent()
        )

        assertFailedWith(
            compile(unannotatedModel, userModel),
            "Field 'address' contains type 'UnannotatedAddress' which is not annotated with @NetworkModel"
        )
    }

    @Test
    fun testJavaClass_subclassWithoutNetworkModelAnnotation_failure() {
        val baseModel = SourceFile.java(
            "BaseResponse.java",
            """
            package com.example;

            import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel;
            import com.google.gson.annotations.SerializedName;

            @NetworkModel
            public class BaseResponse {
                @SerializedName("status")
                private String status;
            }
            """.trimIndent()
        )

        val childModel = SourceFile.java(
            "UserResponse.java",
            """
            package com.example;

            import com.google.gson.annotations.SerializedName;

            public class UserResponse extends BaseResponse {
                @SerializedName("user_id")
                private String userId;
            }
            """.trimIndent()
        )

        assertFailedWith(
            compile(baseModel, childModel),
            "Class 'UserResponse' extends 'BaseResponse' which is annotated with @NetworkModel, but 'UserResponse' is not annotated with @NetworkModel"
        )
    }

    @Test
    fun testJavaClass_listOfUnannotatedCustomType_failure() {
        val unannotatedItem = SourceFile.java(
            "UnannotatedItem.java",
            """
            package com.example;

            public class UnannotatedItem {
                private String id;
                private String name;
            }
            """.trimIndent()
        )

        val orderModel = SourceFile.java(
            "Order.java",
            """
            package com.example;

            import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel;
            import com.google.gson.annotations.SerializedName;
            import java.util.List;

            @NetworkModel
            public class Order {
                @SerializedName("order_id")
                private String orderId;

                @SerializedName("items")
                private List<UnannotatedItem> items;
            }
            """.trimIndent()
        )

        assertFailedWith(
            compile(unannotatedItem, orderModel),
            "Field 'items' contains type 'UnannotatedItem' which is not annotated with @NetworkModel"
        )
    }

    @Test
    fun testJavaClass_mapWithUnannotatedCustomValueType_failure() {
        val unannotatedProduct = SourceFile.java(
            "UnannotatedProduct.java",
            """
            package com.example;

            public class UnannotatedProduct {
                private String sku;
                private double price;
            }
            """.trimIndent()
        )

        val catalogModel = SourceFile.java(
            "Catalog.java",
            """
            package com.example;

            import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel;
            import com.google.gson.annotations.SerializedName;
            import java.util.Map;

            @NetworkModel
            public class Catalog {
                @SerializedName("products")
                private Map<String, UnannotatedProduct> products;
            }
            """.trimIndent()
        )

        assertFailedWith(
            compile(unannotatedProduct, catalogModel),
            "Field 'products' contains type 'UnannotatedProduct' which is not annotated with @NetworkModel"
        )
    }

    @Test
    fun testJavaClass_nestedUnannotatedType_failure() {
        val unannotatedCountry = SourceFile.java(
            "UnannotatedCountry.java",
            """
            package com.example;

            public class UnannotatedCountry {
                private String name;
            }
            """.trimIndent()
        )

        val addressModel = SourceFile.java(
            "Address.java",
            """
            package com.example;

            import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel;
            import com.google.gson.annotations.SerializedName;

            @NetworkModel
            public class Address {
                @SerializedName("street")
                private String street;

                @SerializedName("country")
                private UnannotatedCountry country;
            }
            """.trimIndent()
        )

        assertFailedWith(
            compile(unannotatedCountry, addressModel),
            "Field 'country' contains type 'UnannotatedCountry' which is not annotated with @NetworkModel"
        )
    }

    @Test
    fun testJavaClass_multiLevelInheritance_grandchildNotAnnotated_failure() {
        val baseModel = SourceFile.java(
            "BaseResponse.java",
            """
            package com.example;

            import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel;
            import com.google.gson.annotations.SerializedName;

            @NetworkModel
            public class BaseResponse {
                @SerializedName("status")
                private String status;
            }
            """.trimIndent()
        )

        val childModel = SourceFile.java(
            "EntityResponse.java",
            """
            package com.example;

            import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel;
            import com.google.gson.annotations.SerializedName;

            @NetworkModel
            public class EntityResponse extends BaseResponse {
                @SerializedName("entity_id")
                private String entityId;
            }
            """.trimIndent()
        )

        val grandchildModel = SourceFile.java(
            "UserResponse.java",
            """
            package com.example;

            import com.google.gson.annotations.SerializedName;

            public class UserResponse extends EntityResponse {
                @SerializedName("user_name")
                private String userName;
            }
            """.trimIndent()
        )

        assertFailedWith(
            compile(baseModel, childModel, grandchildModel),
            "Class 'UserResponse' extends 'EntityResponse' which is annotated with @NetworkModel, but 'UserResponse' is not annotated with @NetworkModel"
        )
    }

    @Test
    fun testJavaClass_multipleIssues_failure() {
        val unannotatedType = SourceFile.java(
            "UnannotatedType.java",
            """
            package com.example;

            public class UnannotatedType {
                private String value;
            }
            """.trimIndent()
        )

        val problematicModel = SourceFile.java(
            "ProblematicModel.java",
            """
            package com.example;

            import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel;
            import com.google.gson.annotations.SerializedName;

            @NetworkModel
            public class ProblematicModel {
                @SerializedName("valid_field")
                private String validField;

                private String missingSerializedName;

                @SerializedName("unannotated_type_field")
                private UnannotatedType unannotatedTypeField;
            }
            """.trimIndent()
        )

        assertFailedWith(
            compile(unannotatedType, problematicModel),
            "Missing @SerializedName annotation on field: missingSerializedName",
            "Field 'unannotatedTypeField' contains type 'UnannotatedType' which is not annotated with @NetworkModel"
        )
    }

    @Test
    fun testJavaClass_enumField_success() {
        val enumType = SourceFile.java(
            "Status.java",
            """
            package com.example;

            public enum Status {
                ACTIVE,
                INACTIVE,
                PENDING
            }
            """.trimIndent()
        )

        val modelWithEnum = SourceFile.java(
            "UserWithStatus.java",
            """
            package com.example;

            import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel;
            import com.google.gson.annotations.SerializedName;

            @NetworkModel
            public class UserWithStatus {
                @SerializedName("name")
                private String name;

                @SerializedName("status")
                private Status status;
            }
            """.trimIndent()
        )

        assertFailedWith(
            compile(enumType, modelWithEnum),
            "Field 'status' contains type 'Status' which is not annotated with @NetworkModel"
        )
    }

    @Test
    fun testJavaClass_interfaceFieldType_failure() {
        val interfaceType = SourceFile.java(
            "Identifiable.java",
            """
            package com.example;

            public interface Identifiable {
                String getId();
            }
            """.trimIndent()
        )

        val implementation = SourceFile.java(
            "IdentifiableImpl.java",
            """
            package com.example;

            public class IdentifiableImpl implements Identifiable {
                private String id;
                public String getId() { return id; }
            }
            """.trimIndent()
        )

        val modelWithInterface = SourceFile.java(
            "Container.java",
            """
            package com.example;

            import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel;
            import com.google.gson.annotations.SerializedName;

            @NetworkModel
            public class Container {
                @SerializedName("item")
                private Identifiable item;
            }
            """.trimIndent()
        )

        assertFailedWith(
            compile(interfaceType, implementation, modelWithInterface),
            "Field 'item' contains type 'Identifiable' which is not annotated with @NetworkModel"
        )
    }

    @Test
    fun testJavaClass_innerClassNotAnnotated_failure() {
        val outerClass = SourceFile.java(
            "Outer.java",
            """
            package com.example;

            import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel;
            import com.google.gson.annotations.SerializedName;

            @NetworkModel
            public class Outer {
                @SerializedName("inner")
                private Inner inner;

                public static class Inner {
                    private String value;
                }
            }
            """.trimIndent()
        )

        assertFailedWith(
            compile(outerClass),
            "Field 'inner' contains type 'Inner' which is not annotated with @NetworkModel"
        )
    }

    // ==================== EDGE CASES ====================

    @Test
    fun testJavaClass_innerClassAnnotated_success() {
        val outerClass = SourceFile.java(
            "Outer.java",
            """
            package com.example;

            import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel;
            import com.google.gson.annotations.SerializedName;

            @NetworkModel
            public class Outer {
                @SerializedName("inner")
                private Inner inner;

                @NetworkModel
                public static class Inner {
                    @SerializedName("value")
                    private String value;
                }
            }
            """.trimIndent()
        )

        assertSucceeded(compile(outerClass))
    }

    @Test
    fun testJavaClass_nestedGenericsStandardTypes_success() {
        val source = SourceFile.java(
            "Matrix.java",
            """
            package com.example;

            import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel;
            import com.google.gson.annotations.SerializedName;
            import java.util.List;

            @NetworkModel
            public class Matrix {
                @SerializedName("data")
                private List<List<Integer>> data;
            }
            """.trimIndent()
        )

        assertSucceeded(compile(source))
    }

    @Test
    fun testJavaClass_nestedGenericsUnannotatedCustomType_failure() {
        val cellType = SourceFile.java(
            "Cell.java",
            """
            package com.example;

            public class Cell {
                private int value;
            }
            """.trimIndent()
        )

        val matrixModel = SourceFile.java(
            "Matrix.java",
            """
            package com.example;

            import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel;
            import com.google.gson.annotations.SerializedName;
            import java.util.List;

            @NetworkModel
            public class Matrix {
                @SerializedName("cells")
                private List<List<Cell>> cells;
            }
            """.trimIndent()
        )

        assertFailedWith(
            compile(cellType, matrixModel),
            "Field 'cells' contains type 'Cell' which is not annotated with @NetworkModel"
        )
    }
}
