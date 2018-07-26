package atos.mae.auto.hpalm.infrastructure;

/*

This file was generated by the JavaTM Architecture for XML Binding(JAXB)
Reference Implementation, vhudson-jaxb-ri-2.1-456
See http://www.oracle.com/technetwork/articles/javase/index-140168.html
Any modifications to this file will be lost upon recompilation of the source schema.


This example of an automatically generated class is an example of how one can
generate classes from XSDs via xjc to match jaxb standards.
XSD is a format for describing a class structure
(note: the CLASS not an INSTANCE of the class).
From an XSD one can generate a class java source file.
When compiling this source file, one can "marshal" an actual object instance
from the XML describing the object (this time we are talking about an instance,
not a class).

this process has many advantages, and is a form of serialization that is not
language dependent.
This is the recommended way of working with entities, though we do suggest you
customize your entity class with simpler accessors.


*/

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
* Java class for anonymous complex type.
*
* The following schema fragment specifies the expected content contained within this class.
*
* <complexType>
*   <complexContent>
*     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
*       <sequence>
*         <element name="Fields">
*           <complexType>
*             <complexContent>
*               <restriction base=
*                  "{http://www.w3.org/2001/XMLSchema}anyType">
*                 <sequence>
*                   <element name="Field" maxOccurs="unbounded">
*                     <complexType>
*                       <complexContent>
*                         <restriction base=
*                            "{http://www.w3.org/2001/XMLSchema}anyType">
*                           <sequence>
*                             <element name="Value"
*                               type="{http://www.w3.org/2001/XMLSchema}string"
*                               maxOccurs="unbounded"/>
*                           </sequence>
*                           <attribute name="Name" use="required"
*                             type="{http://www.w3.org/2001/XMLSchema}string" />
*                         </restriction>
*                       </complexContent>
*                     </complexType>
*                   </element>
*                 </sequence>
*               </restriction>
*             </complexContent>
*           </complexType>
*         </element>
*       </sequence>
*       <attribute name="Type" use="required"
*           type="{http://www.w3.org/2001/XMLSchema}string" />
*     </restriction>
*   </complexContent>
* </complexType>
*
*
*/
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "fields" })
@XmlRootElement(name = "Entity")
public class Entity {

   @XmlElement(name = "Fields", required = true)
   protected Entity.Fields fields;
   @XmlAttribute(name = "Type", required = true)
   protected String type;


   public Entity(Entity entity) {
       type = entity.getType();
       fields = new Entity.Fields(entity.getFields());
   }

   public Entity() {}

   /**
    * Gets the value of the fields property.
    *
    * @return possible object is {@link Entity.Fields }
    *
    */
   public Entity.Fields getFields() {
       return fields;
   }

   /**
    * Sets the value of the fields property.
    *
    * @param value
    *            allowed object is {@link Entity.Fields }
    *
    */
   public void setFields(Entity.Fields value) {
       this.fields = value;
   }

   /**
    * Gets the value of the type property.
    *
    * @return possible object is {@link String }
    *
    */
   public String getType() {
       return type;
   }

   /**
    * Sets the value of the type property.
    *
    * @param value
    *            allowed object is {@link String }
    *
    */
   public void setType(String value) {
       this.type = value;
   }

   /**
    * Java class for anonymous complex type.
    *
    * The following schema fragment specifies the expected content contained within this class.
    *
    * <complexType>
    *   <complexContent>
    *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
    *       <sequence>
    *         <element name="Field" maxOccurs="unbounded">
    *           <complexType>
    *             <complexContent>
    *               <restriction base=
    *                  "{http://www.w3.org/2001/XMLSchema}anyType">
    *                 <sequence>
    *                   <element name="Value"
    *                     type="{http://www.w3.org/2001/XMLSchema}string"
    *                       maxOccurs="unbounded"/>
    *                 </sequence>
    *                 <attribute name="Name" use="required"
    *                   type="{http://www.w3.org/2001/XMLSchema}string" />
    *               </restriction>
    *             </complexContent>
    *           </complexType>
    *         </element>
    *       </sequence>
    *     </restriction>
    *   </complexContent>
    * </complexType>
    *
    *
    */
   @XmlAccessorType(XmlAccessType.FIELD)
   @XmlType(name = "", propOrder = { "field" })
   public static class Fields {

       @XmlElement(name = "Field", required = true)
       protected List<Field> field;


       public Fields(Fields fields) {
           field = new ArrayList<Field>(fields.getField());
       }


       public Fields() {}

       /**
        * Gets the value of the field property.
        *

        * This accessor method returns a reference to the live list, not a snapshot.
        * Therefore any  modification you make to the returned list will be present
        * inside the JAXB object.
        * This is why there is no set method for the field property.
        *
        * For example, to add a new item, do as follows:
        *
        *  getField().add(newItem);
        *
        * Objects of the following type(s) are allowed in the list {@link Entity.Fields.Field }
        *
        *
        */
       public List<Field> getField() {
           if (field == null) {
               field = new ArrayList<Field>();
           }
           return this.field;
       }

       /**
        * Java class for anonymous complex type.
        *
        * The following schema fragment specifies the expected content contained
        * within this class.
        *
        * <complexType>
        *   <complexContent>
        *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
        *       <sequence>
        *         <element name="Value"
        *            type="{http://www.w3.org/2001/XMLSchema}string"
        *            maxOccurs="unbounded"/>
        *       </sequence>
        *       <attribute name="Name" use="required"
        *          type="{http://www.w3.org/2001/XMLSchema}string" />
        *     </restriction>
        *   </complexContent>
        * </complexType>
        *
        */
       @XmlAccessorType(XmlAccessType.FIELD)
       @XmlType(name = "", propOrder = { "value" })
       public static class Field {

           @XmlElement(name = "Value", required = true)
           protected List<String> value;
           @XmlAttribute(name = "Name", required = true)
           protected String name;

           /**
            * Gets the value of the value property.
            *
            * This accessor method returns a reference to the live list, not a snapshot.
            * Therefore, any modification you make to the returned list will be present
            * inside the JAXB object. This is why there is no set method
            * for the value property.
            *
            * For example, to add a new item, do as follows:
            *
            * getValue().add(newItem);
            *

            * Objects of the following type(s) are allowed in the list {@link String }
            *
            *
            */
           public List<String> getValue() {
               if (value == null) {
                   value = new ArrayList<String>();
               }
               return this.value;
           }

           /**
            * Gets the value of the name property.
            *
            * @return possible object is {@link String }
            *
            */
           public String getName() {
               return name;
           }

           /**
            * Sets the value of the name property.
            *
            * @param value
            *            allowed object is {@link String }
            *
            */
           public void setName(String value) {
               this.name = value;
           }

       }

   }

}
