/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tartarus.snowball;


/**
 *
 * @author malsulmi
 */
public class SnowballStemmerExample {

    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class stemClass = Class.forName("org.tartarus.snowball.ext." + "arabicStemmer");
        //Class stemClass = Class.forName("org.tartarus.snowball.ext." + "englishStemmer");  //it is called porter2

        SnowballStemmer stemmer = (SnowballStemmer) stemClass.newInstance();

        String text = "مدة التمويل قبل الجدوله اربع سنوات وتتضمن فترة سماح سنة واحده من تاريخ الشراء  مدة التمويل بعد الجدوله خمسة سنوات وتتضمن فترة سماح سنة واحده من تاريخ الشراء  تمويل مرابحة التورق بالمعادن بمبلغ 14,350,390.72 ريال سعودي  مدة التمويل قبل الجدوله خمسة سنوات وتتضمن فترة سماح سنة واحده من تاريخ الشراء  مدة التمويل بعد الجدوله ستة سنوات وتتضمن فترة سماح سنة واحده من تاريخ الشراء بند توضيح مقدمة يسر شركة مجموعة عبدالمحسن الحكير للسياحة والتنمية أن تعلن للمساهمين الكرام عن توقيع اتفاقية إعادة جدولة التسهيلات المصرفية الاسلاميه مع البنك السعودي البريطاني (ساب) والذي سيكون له الأثر الإيجابي على ادارة التدفقات النقدية. تاريخ توقيع إعادة جدولة التمويل 1442-07-05 الموافق 2021-02-17 الجهة الممولة البنك السعودي البريطاني ( ساب ) أسباب إعادة جدولة التمويل بما يتناسب مع التدفقات النقديه للشركة قيمة التمويل والجزء المعاد جدولته كامل مبلغ التسهيلات والبالغ قيمته 89,024,485.52 ريال سعودي مدة التمويل قبل الجدولة وبعدها تمويل مرابحة التورق بالمعادن بمبلغ 74,674,094.80 ريال سعودي مدة التمويل قبل الجدوله اربع سنوات وتتضمن فترة سماح سنة واحده من تاريخ الشراء مدة التمويل بعد الجدوله خمسة سنوات وتتضمن فترة سماح سنة واحده من تاريخ الشراء تمويل مرابحة التورق بالمعادن بمبلغ 14,350,390.72 ريال سعودي مدة التمويل قبل الجدوله خمسة سنوات وتتضمن فترة سماح سنة واحده من تاريخ الشراء مدة التمويل بعد الجدوله ستة سنوات وتتضمن فترة سماح سنة واحده من تاريخ الشراء الضمانات المقدمة مقابل التمويل المعاد جدولته سند لامر أطراف ذات علاقة لايوجد معلومات اضافية لايوجد ";
        String[] splittedText = text.split("\\s+");

        for (String term : splittedText) {
            stemmer.setCurrent(term);
            stemmer.stem();
            System.out.print(stemmer.getCurrent() + " ");
        }

        System.out.println();

    }

}
