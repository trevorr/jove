/*
 * jnicpp - Lightweight C++ Wrapper Classes for the Java (TM) Native Interface
 * Copyright (C) 2003 Trevor A. Robinson
 * Java is a registered trademark of Sun Microsystems, Inc. in the U.S. or
 * other countries.
 *
 * Licensed under the Open Software License version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You should
 * have received a copy of the License along with this software; if not, you
 * may obtain a copy of the License at
 *
 * http://opensource.org/licenses/osl-2.0.php
 *
 * This software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

#include <stdlib.h>
#include <assert.h>

#ifdef JNICPP_DEBUG_REF
#include <stdio.h>
#endif

namespace jnicpp {

//////////////////////////////////////////////////////////////////////

void checkException(JNIEnv* penv) throw(JException);
void convertException(JNIEnv* penv, const std::string& msg) throw(JException);
const char* getJNIResultMessage(jint result) throw();

//////////////////////////////////////////////////////////////////////

template<class ClassT>
JRef<ClassT>::JRef(JNIEnv* penv, ClassT obj) :
    m_penv(penv),
    m_obj(obj)
{
    // done
}

template<class ClassT>
JRef<ClassT>::~JRef()
{
    // done
}

template<class ClassT>
JRef<ClassT>::operator ClassT() const throw()
{
    return m_obj;
}

template<class ClassT>
ClassT JRef<ClassT>::release() throw()
{
    jobject obj = m_obj;
    m_obj = NULL;
    return obj;
}

template<class ClassT>
JNIEnv* JRef<ClassT>::getEnv() const throw()
{
    return m_penv;
}

//////////////////////////////////////////////////////////////////////

template<class ClassT>
JLocalRef<ClassT>::JLocalRef(JNIEnv* penv, ClassT obj) :
    JRef<ClassT>(penv, obj)
{
    // done
}

template<class ClassT>
JLocalRef<ClassT>::~JLocalRef()
{
    if (this->m_obj != NULL) {
#ifdef JNICPP_DEBUG_REF
        printf("DeleteLocalRef: penv=%p, lref=%p\n", m_penv, m_obj);
#endif
        this->m_penv->DeleteLocalRef(this->m_obj);
    }
}

template<class ClassT>
ClassT JLocalRef<ClassT>::newLocalRef(JNIEnv* penv, ClassT obj)
{
    if (obj != NULL) {
        ClassT ref = static_cast<ClassT>(penv->NewLocalRef(obj));
#ifdef JNICPP_DEBUG_REF
        printf("NewLocalRef: penv=%p, ref=%p, lref=%p\n", penv, obj, ref);
#endif
        if (ref == NULL) {
            convertException(penv, "Unable to create new local reference");
        }
        return ref;
    } else {
        return NULL;
    }
}

//////////////////////////////////////////////////////////////////////

template<class ClassT>
JGlobalRef<ClassT>::JGlobalRef(JNIEnv* penv, ClassT obj, bool freeLocal) :
    JRef<ClassT>(penv, newGlobalRef(penv, obj, freeLocal))
{
    // done
}

template<class ClassT>
JGlobalRef<ClassT>::JGlobalRef(const JGlobalRef<ClassT>& that) :
    JRef<ClassT>(that.getEnv(), newGlobalRef(that.getEnv(), that, false))
{
    // done
}

template<class ClassT>
JGlobalRef<ClassT>& JGlobalRef<ClassT>::operator=(const JGlobalRef<ClassT>& that)
{
    if (*this != that) {
        assignRef(that.m_penv, that.m_obj);
    }
    return *this;
}

template<class ClassT>
JGlobalRef<ClassT>::~JGlobalRef()
{
    if (this->m_obj != NULL) {
#ifdef JNICPP_DEBUG_REF
        printf("DeleteGlobalRef: penv=%p, gref=%p\n", m_penv, m_obj);
#endif
        this->m_penv->DeleteGlobalRef(this->m_obj);
    }
}

template<class ClassT>
ClassT JGlobalRef<ClassT>::newGlobalRef(JNIEnv* penv, ClassT obj, bool freeLocal)
{
    if (obj != NULL) {
        ClassT ref = static_cast<ClassT>(penv->NewGlobalRef(obj));
#ifdef JNICPP_DEBUG_REF
        printf("NewGlobalRef: penv=%p, lref=%p, gref=%p\n", penv, obj, ref);
#endif
        if (ref == NULL) {
            convertException(penv, "Unable to create new global reference");
        }
        if (freeLocal) {
#ifdef JNICPP_DEBUG_REF
            printf("DeleteLocalRef: penv=%p, lref=%p\n", penv, obj);
#endif
            penv->DeleteLocalRef(obj);
        }
        return ref;
    } else {
        return NULL;
    }
}

template<class ClassT>
void JGlobalRef<ClassT>::assignRef(JNIEnv* penv, ClassT obj)
{
    if (this->m_obj != obj) {
        // copy new reference first, for exception safety
        ClassT newRef = newGlobalRef(penv, obj, false);

        // delete old reference
        if (this->m_obj != NULL) {
#ifdef JNICPP_DEBUG_REF
            printf("DeleteGlobalRef: penv=%p, gref=%p\n", this->m_penv, this->m_obj);
#endif
            this->m_penv->DeleteGlobalRef(this->m_obj);
        }

        // assign new reference
        this->m_penv = penv;
        this->m_obj = newRef;
    }
}

//////////////////////////////////////////////////////////////////////

template<class ClassT>
JObjectTmpl<ClassT>::JObjectTmpl(JNIEnv* penv, ClassT obj, bool freeLocal) :
    base_type(penv, obj, freeLocal)
{
    // done
}

#ifndef _MSC_VER
template<class ClassT>
JObjectTmpl<ClassT>::JObjectTmpl(const JObjectTmpl<ClassT>& that) :
    base_type(that)
{
    // done
}
#endif

template<class ClassT>
jclass JObjectTmpl<ClassT>::getClass() const throw()
{
    return this->m_penv->GetObjectClass(this->m_obj);
}

template<class ClassT>
bool JObjectTmpl<ClassT>::isInstanceOf(jclass cls) const throw()
{
    return this->m_penv->IsInstanceOf(this->m_obj, cls);
}

template<class ClassT>
bool JObjectTmpl<ClassT>::isSameObject(jobject that) const throw()
{
    return this->m_penv->IsSameObject(this->m_obj, that);
}

//////////////////////////////////////////////////////////////////////

inline JClass::JClass(JNIEnv* penv, jclass obj, bool freeLocal) :
    base_type(penv, obj, freeLocal)
{
    // done
}

inline JClass::JClass(JNIEnv* penv, const char* name) :
    base_type(penv, findClass(penv, name), true)
{
    // done
}

template <class ReturnT>
JMethod<ReturnT> JClass::getMethod(const char* name, const char* signature) const
{
    return JMethod<ReturnT>(*this, name, signature);
}

template <class ReturnT>
JStaticMethod<ReturnT> JClass::getStaticMethod(const char* name, const char* signature) const
{
    return JStaticMethod<ReturnT>(*this, name, signature);
}

template <class ClassT>
JCtorTmpl<ClassT> JClass::getCtorTmpl(const char* signature) const
{
    return JCtorTmpl<ClassT>(*this, signature);
}

template <class FieldT>
JField<FieldT> JClass::getField(const char* name, const char* signature) const
{
    return JField<FieldT>(*this, name, signature);
}

template <class FieldT>
JStaticField<FieldT> JClass::getStaticField(const char* name, const char* signature) const
{
    return JStaticField<FieldT>(*this, name, signature);
}

//////////////////////////////////////////////////////////////////////

template <int MaxArgs>
JArgumentsN<MaxArgs>::JArgumentsN() :
    m_cur(0)
{
    // done
}

template <int MaxArgs>
JArgumentsN<MaxArgs>& JArgumentsN<MaxArgs>::operator<<(jboolean z) throw()
{
    assert(m_cur < MaxArgs);
    m_values[m_cur++].z = z;
    return *this;
}

template <int MaxArgs>
JArgumentsN<MaxArgs>& JArgumentsN<MaxArgs>::operator<<(jbyte b) throw()
{
    assert(m_cur < MaxArgs);
    m_values[m_cur++].b = b;
    return *this;
}

template <int MaxArgs>
JArgumentsN<MaxArgs>& JArgumentsN<MaxArgs>::operator<<(jchar c) throw()
{
    assert(m_cur < MaxArgs);
    m_values[m_cur++].c = c;
    return *this;
}

template <int MaxArgs>
JArgumentsN<MaxArgs>& JArgumentsN<MaxArgs>::operator<<(jshort s) throw()
{
    assert(m_cur < MaxArgs);
    m_values[m_cur++].s = s;
    return *this;
}

template <int MaxArgs>
JArgumentsN<MaxArgs>& JArgumentsN<MaxArgs>::operator<<(jint i) throw()
{
    assert(m_cur < MaxArgs);
    m_values[m_cur++].i = i;
    return *this;
}

template <int MaxArgs>
JArgumentsN<MaxArgs>& JArgumentsN<MaxArgs>::operator<<(jlong j) throw()
{
    assert(m_cur < MaxArgs);
    m_values[m_cur++].j = j;
    return *this;
}

template <int MaxArgs>
JArgumentsN<MaxArgs>& JArgumentsN<MaxArgs>::operator<<(jfloat f) throw()
{
    assert(m_cur < MaxArgs);
    m_values[m_cur++].f = f;
    return *this;
}

template <int MaxArgs>
JArgumentsN<MaxArgs>& JArgumentsN<MaxArgs>::operator<<(jdouble d) throw()
{
    assert(m_cur < MaxArgs);
    m_values[m_cur++].d = d;
    return *this;
}

template <int MaxArgs>
JArgumentsN<MaxArgs>& JArgumentsN<MaxArgs>::operator<<(jobject l) throw()
{
    assert(m_cur < MaxArgs);
    m_values[m_cur++].l = l;
    return *this;
}

template <int MaxArgs>
JArgumentsN<MaxArgs>::operator jvalue*() throw()
{
    return m_values;
}

//////////////////////////////////////////////////////////////////////

inline JAbstractMethod::JAbstractMethod(const JClass& cls, jmethodID methodID) :
    m_cls(cls),
    m_methodID(methodID)
{
    // done
}

inline JAbstractMethod::operator jmethodID() const throw()
{
    return m_methodID;
}

//////////////////////////////////////////////////////////////////////

template <class ReturnT>
JMethod<ReturnT>::JMethod(const JClass& cls, jmethodID methodID) :
    JAbstractMethod(cls, methodID)
{
    // done
}

template <class ReturnT>
JMethod<ReturnT>::JMethod(const JClass& cls, const char* name, const char* signature) :
    JAbstractMethod(cls, getMethodID(cls.getEnv(), cls, name, signature))
{
    // done
}

template <class ReturnT>
ReturnT JMethod<ReturnT>::call(jobject obj, jvalue* args) const
{
    return callMethod(m_cls.getEnv(), obj, m_methodID, args);
}

template <class ReturnT>
ReturnT JMethod<ReturnT>::callMethod(JNIEnv* penv, jobject obj, jmethodID methodID,
    jvalue* args)
{
    ReturnT result = static_cast<ReturnT>(penv->CallObjectMethodA(obj, methodID, args));
#ifdef JNICPP_DEBUG_REF
    printf("CallObjectMethodA: penv=%p, obj=%p, methodID=%p, result=%p\n",
        penv, obj, methodID, result);
#endif
    checkException(penv);
    return result;
}

template <>
inline void JMethod<void>::callMethod(JNIEnv* penv, jobject obj, jmethodID methodID,
    jvalue* args)
{
    penv->CallVoidMethodA(obj, methodID, args);
    checkException(penv);
}

template <>
inline jboolean JMethod<jboolean>::callMethod(JNIEnv* penv, jobject obj, jmethodID methodID,
    jvalue* args)
{
    jboolean result = penv->CallBooleanMethodA(obj, methodID, args);
    checkException(penv);
    return result;
}

template <>
inline jbyte JMethod<jbyte>::callMethod(JNIEnv* penv, jobject obj, jmethodID methodID,
    jvalue* args)
{
    jbyte result = penv->CallByteMethodA(obj, methodID, args);
    checkException(penv);
    return result;
}

template <>
inline jchar JMethod<jchar>::callMethod(JNIEnv* penv, jobject obj, jmethodID methodID,
    jvalue* args)
{
    jchar result = penv->CallCharMethodA(obj, methodID, args);
    checkException(penv);
    return result;
}

template <>
inline jshort JMethod<jshort>::callMethod(JNIEnv* penv, jobject obj, jmethodID methodID,
    jvalue* args)
{
    jshort result = penv->CallShortMethodA(obj, methodID, args);
    checkException(penv);
    return result;
}

template <>
inline jint JMethod<jint>::callMethod(JNIEnv* penv, jobject obj, jmethodID methodID,
    jvalue* args)
{
    jint result = penv->CallIntMethodA(obj, methodID, args);
    checkException(penv);
    return result;
}

template <>
inline jlong JMethod<jlong>::callMethod(JNIEnv* penv, jobject obj, jmethodID methodID,
    jvalue* args)
{
    jlong result = penv->CallLongMethodA(obj, methodID, args);
    checkException(penv);
    return result;
}

template <>
inline jfloat JMethod<jfloat>::callMethod(JNIEnv* penv, jobject obj, jmethodID methodID,
    jvalue* args)
{
    jfloat result = penv->CallFloatMethodA(obj, methodID, args);
    checkException(penv);
    return result;
}

template <>
inline jdouble JMethod<jdouble>::callMethod(JNIEnv* penv, jobject obj, jmethodID methodID,
    jvalue* args)
{
    jdouble result = penv->CallDoubleMethodA(obj, methodID, args);
    checkException(penv);
    return result;
}

//////////////////////////////////////////////////////////////////////

template <class ReturnT>
JStaticMethod<ReturnT>::JStaticMethod(const JClass& cls, jmethodID methodID) :
    JAbstractMethod(cls, methodID)
{
    // done
}

template <class ReturnT>
JStaticMethod<ReturnT>::JStaticMethod(const JClass& cls, const char* name, const char* signature) :
    JAbstractMethod(cls, getStaticMethodID(cls.getEnv(), cls, name, signature))
{
    // done
}

template <class ReturnT>
ReturnT JStaticMethod<ReturnT>::call(jvalue* args) const
{
    return callStaticMethod(m_cls.getEnv(), m_cls, m_methodID, args);
}

template <class ReturnT>
ReturnT JStaticMethod<ReturnT>::callStaticMethod(JNIEnv* penv, jclass cls, jmethodID methodID,
    jvalue* args)
{
    ReturnT result = static_cast<ReturnT>(penv->CallStaticObjectMethodA(cls, methodID, args));
#ifdef JNICPP_DEBUG_REF
    printf("CallStaticObjectMethodA: penv=%p, cls=%p, methodID=%p, result=%p\n",
        penv, cls, methodID, result);
#endif
    checkException(penv);
    return result;
}

template <>
inline void JStaticMethod<void>::callStaticMethod(JNIEnv* penv, jclass cls, jmethodID methodID,
    jvalue* args)
{
    penv->CallStaticVoidMethodA(cls, methodID, args);
    checkException(penv);
}

template <>
inline jboolean JStaticMethod<jboolean>::callStaticMethod(JNIEnv* penv, jclass cls, jmethodID methodID,
    jvalue* args)
{
    jboolean result = penv->CallStaticBooleanMethodA(cls, methodID, args);
    checkException(penv);
    return result;
}

template <>
inline jbyte JStaticMethod<jbyte>::callStaticMethod(JNIEnv* penv, jclass cls, jmethodID methodID,
    jvalue* args)
{
    jbyte result = penv->CallStaticByteMethodA(cls, methodID, args);
    checkException(penv);
    return result;
}

template <>
inline jchar JStaticMethod<jchar>::callStaticMethod(JNIEnv* penv, jclass cls, jmethodID methodID,
    jvalue* args)
{
    jchar result = penv->CallStaticCharMethodA(cls, methodID, args);
    checkException(penv);
    return result;
}

template <>
inline jshort JStaticMethod<jshort>::callStaticMethod(JNIEnv* penv, jclass cls, jmethodID methodID,
    jvalue* args)
{
    jshort result = penv->CallStaticShortMethodA(cls, methodID, args);
    checkException(penv);
    return result;
}

template <>
inline jint JStaticMethod<jint>::callStaticMethod(JNIEnv* penv, jclass cls, jmethodID methodID,
    jvalue* args)
{
    jint result = penv->CallStaticIntMethodA(cls, methodID, args);
    checkException(penv);
    return result;
}

template <>
inline jlong JStaticMethod<jlong>::callStaticMethod(JNIEnv* penv, jclass cls, jmethodID methodID,
    jvalue* args)
{
    jlong result = penv->CallStaticLongMethodA(cls, methodID, args);
    checkException(penv);
    return result;
}

template <>
inline jfloat JStaticMethod<jfloat>::callStaticMethod(JNIEnv* penv, jclass cls, jmethodID methodID,
    jvalue* args)
{
    jfloat result = penv->CallStaticFloatMethodA(cls, methodID, args);
    checkException(penv);
    return result;
}

template <>
inline jdouble JStaticMethod<jdouble>::callStaticMethod(JNIEnv* penv, jclass cls, jmethodID methodID,
    jvalue* args)
{
    jdouble result = penv->CallStaticDoubleMethodA(cls, methodID, args);
    checkException(penv);
    return result;
}

//////////////////////////////////////////////////////////////////////

template <class ClassT>
JCtorTmpl<ClassT>::JCtorTmpl(const JClass& cls, jmethodID methodID) :
    JAbstractMethod(cls, methodID)
{
    // done
}

template <class ClassT>
JCtorTmpl<ClassT>::JCtorTmpl(const JClass& cls, const char* signature) :
    JAbstractMethod(cls, getMethodID(cls.getEnv(), cls, "<init>", signature))
{
    // done
}

template <class ClassT>
ClassT JCtorTmpl<ClassT>::create(jvalue* args) const
{
    return newObject(m_cls.getEnv(), m_cls, m_methodID, args);
}

template <class ClassT>
JObjectTmpl<ClassT> JCtorTmpl<ClassT>::createObject(jvalue* args) const
{
    JNIEnv* penv = m_cls.getEnv();
    return JObjectTmpl<ClassT>(penv, newObject(penv, m_cls, m_methodID, args), true);
}

template <class ClassT>
ClassT JCtorTmpl<ClassT>::newObject(JNIEnv* penv, jclass cls, jmethodID methodID,
    jvalue* args)
{
    ClassT obj = static_cast<ClassT>(penv->NewObjectA(cls, methodID, args));
#ifdef JNICPP_DEBUG_REF
    printf("NewObjectA: penv=%p, cls=%p, methodID=%p, obj=%p\n",
        penv, cls, methodID, obj);
#endif
    if (obj == NULL) {
        convertException(penv, "Unable to create new object");
    }
    return obj;
}

//////////////////////////////////////////////////////////////////////

inline JAbstractField::JAbstractField(const JClass& cls, jfieldID fieldID) :
    m_cls(cls),
    m_fieldID(fieldID)
{
    // done
}

inline JAbstractField::operator jfieldID() const throw()
{
    return m_fieldID;
}

//////////////////////////////////////////////////////////////////////

template <class FieldT>
JField<FieldT>::JField(const JClass& cls, jfieldID fieldID) :
    JAbstractField(cls, fieldID)
{
    // done
}

template <class FieldT>
JField<FieldT>::JField(const JClass& cls, const char* name, const char* signature) :
    JAbstractField(cls, getFieldID(cls.getEnv(), cls, name, signature))
{
    // done
}

template <class FieldT>
FieldT JField<FieldT>::value(jobject obj) const
{
    return getField(m_cls.getEnv(), obj, m_fieldID);
}

template <class FieldT>
FieldT JField<FieldT>::getField(JNIEnv* penv, jobject obj, jfieldID fieldID)
{
    FieldT result = static_cast<FieldT>(penv->GetObjectField(obj, fieldID));
#ifdef JNICPP_DEBUG_REF
    printf("GetObjectField: penv=%p, obj=%p, fieldID=%p, result=%p\n",
        penv, obj, fieldID, result);
#endif
    checkException(penv);
    return result;
}

template <>
inline jboolean JField<jboolean>::getField(JNIEnv* penv, jobject obj, jfieldID fieldID)
{
    jboolean result = penv->GetBooleanField(obj, fieldID);
    checkException(penv);
    return result;
}

template <>
inline jbyte JField<jbyte>::getField(JNIEnv* penv, jobject obj, jfieldID fieldID)
{
    jbyte result = penv->GetByteField(obj, fieldID);
    checkException(penv);
    return result;
}

template <>
inline jchar JField<jchar>::getField(JNIEnv* penv, jobject obj, jfieldID fieldID)
{
    jchar result = penv->GetCharField(obj, fieldID);
    checkException(penv);
    return result;
}

template <>
inline jshort JField<jshort>::getField(JNIEnv* penv, jobject obj, jfieldID fieldID)
{
    jshort result = penv->GetShortField(obj, fieldID);
    checkException(penv);
    return result;
}

template <>
inline jint JField<jint>::getField(JNIEnv* penv, jobject obj, jfieldID fieldID)
{
    jint result = penv->GetIntField(obj, fieldID);
    checkException(penv);
    return result;
}

template <>
inline jlong JField<jlong>::getField(JNIEnv* penv, jobject obj, jfieldID fieldID)
{
    jlong result = penv->GetLongField(obj, fieldID);
    checkException(penv);
    return result;
}

template <>
inline jfloat JField<jfloat>::getField(JNIEnv* penv, jobject obj, jfieldID fieldID)
{
    jfloat result = penv->GetFloatField(obj, fieldID);
    checkException(penv);
    return result;
}

template <>
inline jdouble JField<jdouble>::getField(JNIEnv* penv, jobject obj, jfieldID fieldID)
{
    jdouble result = penv->GetDoubleField(obj, fieldID);
    checkException(penv);
    return result;
}

//////////////////////////////////////////////////////////////////////

template <class FieldT>
JStaticField<FieldT>::JStaticField(const JClass& cls, jfieldID fieldID) :
    JAbstractField(cls, fieldID)
{
    // done
}

template <class FieldT>
JStaticField<FieldT>::JStaticField(const JClass& cls, const char* name, const char* signature) :
    JAbstractField(cls, getStaticFieldID(cls.getEnv(), cls, name, signature))
{
    // done
}

template <class FieldT>
FieldT JStaticField<FieldT>::value() const
{
    return getStaticField(m_cls.getEnv(), m_cls, m_fieldID);
}

template <class FieldT>
FieldT JStaticField<FieldT>::getStaticField(JNIEnv* penv, jclass cls, jfieldID fieldID)
{
    FieldT result = static_cast<FieldT>(penv->GetStaticObjectField(cls, fieldID));
#ifdef JNICPP_DEBUG_REF
    printf("GetStaticObjectField: penv=%p, cls=%p, fieldID=%p, result=%p\n",
        penv, cls, fieldID, result);
#endif
    checkException(penv);
    return result;
}

template <>
inline jboolean JStaticField<jboolean>::getStaticField(JNIEnv* penv, jclass cls, jfieldID fieldID)
{
    jboolean result = penv->GetStaticBooleanField(cls, fieldID);
    checkException(penv);
    return result;
}

template <>
inline jbyte JStaticField<jbyte>::getStaticField(JNIEnv* penv, jclass cls, jfieldID fieldID)
{
    jbyte result = penv->GetStaticByteField(cls, fieldID);
    checkException(penv);
    return result;
}

template <>
inline jchar JStaticField<jchar>::getStaticField(JNIEnv* penv, jclass cls, jfieldID fieldID)
{
    jchar result = penv->GetStaticCharField(cls, fieldID);
    checkException(penv);
    return result;
}

template <>
inline jshort JStaticField<jshort>::getStaticField(JNIEnv* penv, jclass cls, jfieldID fieldID)
{
    jshort result = penv->GetStaticShortField(cls, fieldID);
    checkException(penv);
    return result;
}

template <>
inline jint JStaticField<jint>::getStaticField(JNIEnv* penv, jclass cls, jfieldID fieldID)
{
    jint result = penv->GetStaticIntField(cls, fieldID);
    checkException(penv);
    return result;
}

template <>
inline jlong JStaticField<jlong>::getStaticField(JNIEnv* penv, jclass cls, jfieldID fieldID)
{
    jlong result = penv->GetStaticLongField(cls, fieldID);
    checkException(penv);
    return result;
}

template <>
inline jfloat JStaticField<jfloat>::getStaticField(JNIEnv* penv, jclass cls, jfieldID fieldID)
{
    jfloat result = penv->GetStaticFloatField(cls, fieldID);
    checkException(penv);
    return result;
}

template <>
inline jdouble JStaticField<jdouble>::getStaticField(JNIEnv* penv, jclass cls, jfieldID fieldID)
{
    jdouble result = penv->GetStaticDoubleField(cls, fieldID);
    checkException(penv);
    return result;
}

//////////////////////////////////////////////////////////////////////

inline JString::JString(JNIEnv* penv, jstring obj, bool freeLocal) :
    base_type(penv, obj, freeLocal)
{
    // done
}

inline JString::JString(JNIEnv* penv, const char* str) :
    base_type(penv, newStringNonNull(penv, str), true)
{
    // done
}

inline jstring JString::newString(JNIEnv* penv, const char* str)
{
    return (str != NULL) ? newStringNonNull(penv, str) : NULL;
}

inline jstring JString::newStringNonNull(JNIEnv* penv, const char* str)
{
    assert(str != NULL);
    jstring ref = penv->NewStringUTF(str);
#ifdef JNICPP_DEBUG_REF
    printf("NewStringUTF: penv=%p, str=%s, ref=%p\n", penv, str, ref);
#endif
    if (ref == NULL) {
        convertException(penv, "Unable to create new string");
    }
    return ref;
}

inline std::string JString::toString() const
{
    return convertToString(m_penv, m_obj);
}

inline char* JString::toCharArray() const
{
    return convertToCharArray(m_penv, m_obj);
}

inline std::string JString::convertToString(JNIEnv* penv, jstring obj)
{
    const char* chars = penv->GetStringUTFChars(obj, NULL);
    if (chars == NULL) {
        convertException(penv, "Unable to access string characters");
    }
    std::string result(chars);
    penv->ReleaseStringUTFChars(obj, chars);
    return result;
}

inline char* JString::convertToCharArray(JNIEnv* penv, jstring obj)
{
    const char* chars = penv->GetStringUTFChars(obj, NULL);
    if (chars == NULL) {
        convertException(penv, "Unable to access string characters");
    }
    char* result = strdup(chars);
    penv->ReleaseStringUTFChars(obj, chars);
    return result;
}

//////////////////////////////////////////////////////////////////////

template <class ElemT, class ArrayT>
JArray<ElemT, ArrayT>::JArray(JNIEnv* penv, ArrayT obj, bool freeLocal) :
    base_type(penv, obj, freeLocal)
{
    // done
}

template <class ElemT, class ArrayT>
JArray<ElemT, ArrayT>::JArray(JNIEnv* penv, jsize length) :
    base_type(penv, newArray(penv, length), true)
{
    // done
}

template <class ElemT, class ArrayT>
jsize JArray<ElemT, ArrayT>::length()
{
    return this->m_penv->GetArrayLength(this->m_obj);
}

template <>
inline jbooleanArray JArray<jboolean, jbooleanArray>::newArray(JNIEnv* penv, jsize length)
{
    jbooleanArray ref = penv->NewBooleanArray(length);
#ifdef JNICPP_DEBUG_REF
    printf("NewBooleanArray: penv=%p, length=%d, ref=%p\n", penv, length, ref);
#endif
    if (ref == NULL) {
        convertException(penv, "Unable to create new array");
    }
    return ref;
}

template <>
inline jbyteArray JArray<jbyte, jbyteArray>::newArray(JNIEnv* penv, jsize length)
{
    jbyteArray ref = penv->NewByteArray(length);
#ifdef JNICPP_DEBUG_REF
    printf("NewByteArray: penv=%p, length=%d, ref=%p\n", penv, length, ref);
#endif
    if (ref == NULL) {
        convertException(penv, "Unable to create new array");
    }
    return ref;
}

template <>
inline jcharArray JArray<jchar, jcharArray>::newArray(JNIEnv* penv, jsize length)
{
    jcharArray ref = penv->NewCharArray(length);
#ifdef JNICPP_DEBUG_REF
    printf("NewCharArray: penv=%p, length=%d, ref=%p\n", penv, length, ref);
#endif
    if (ref == NULL) {
        convertException(penv, "Unable to create new array");
    }
    return ref;
}

template <>
inline jshortArray JArray<jshort, jshortArray>::newArray(JNIEnv* penv, jsize length)
{
    jshortArray ref = penv->NewShortArray(length);
#ifdef JNICPP_DEBUG_REF
    printf("NewShortArray: penv=%p, length=%d, ref=%p\n", penv, length, ref);
#endif
    if (ref == NULL) {
        convertException(penv, "Unable to create new array");
    }
    return ref;
}

template <>
inline jintArray JArray<jint, jintArray>::newArray(JNIEnv* penv, jsize length)
{
    jintArray ref = penv->NewIntArray(length);
#ifdef JNICPP_DEBUG_REF
    printf("NewIntArray: penv=%p, length=%d, ref=%p\n", penv, length, ref);
#endif
    if (ref == NULL) {
        convertException(penv, "Unable to create new array");
    }
    return ref;
}

template <>
inline jlongArray JArray<jlong, jlongArray>::newArray(JNIEnv* penv, jsize length)
{
    jlongArray ref = penv->NewLongArray(length);
#ifdef JNICPP_DEBUG_REF
    printf("NewLongArray: penv=%p, length=%d, ref=%p\n", penv, length, ref);
#endif
    if (ref == NULL) {
        convertException(penv, "Unable to create new array");
    }
    return ref;
}

template <>
inline jfloatArray JArray<jfloat, jfloatArray>::newArray(JNIEnv* penv, jsize length)
{
    jfloatArray ref = penv->NewFloatArray(length);
#ifdef JNICPP_DEBUG_REF
    printf("NewFloatArray: penv=%p, length=%d, ref=%p\n", penv, length, ref);
#endif
    if (ref == NULL) {
        convertException(penv, "Unable to create new array");
    }
    return ref;
}

template <>
inline jdoubleArray JArray<jdouble, jdoubleArray>::newArray(JNIEnv* penv, jsize length)
{
    jdoubleArray ref = penv->NewDoubleArray(length);
#ifdef JNICPP_DEBUG_REF
    printf("NewDoubleArray: penv=%p, length=%d, ref=%p\n", penv, length, ref);
#endif
    if (ref == NULL) {
        convertException(penv, "Unable to create new array");
    }
    return ref;
}

//////////////////////////////////////////////////////////////////////

template <class ElemT, class ArrayT>
JArrayElements<ElemT, ArrayT>::JArrayElements(const JArray<ElemT, ArrayT>& array) :
    m_array(array),
    m_elements(getElements(array.getEnv(), array))
{
    // done
}

template <class ElemT, class ArrayT>
JArrayElements<ElemT, ArrayT>::~JArrayElements()
{
    if (m_elements != NULL) {
        release();
    }
}

template <class ElemT, class ArrayT>
JArrayElements<ElemT, ArrayT>::operator const ElemT*() const
{
    assert(m_elements != NULL);
    return m_elements;
}

template <class ElemT, class ArrayT>
JArrayElements<ElemT, ArrayT>::operator ElemT*()
{
    assert(m_elements != NULL);
    return m_elements;
}

template <class ElemT, class ArrayT>
const ElemT& JArrayElements<ElemT, ArrayT>::operator[](jsize index) const
{
    assert(m_elements != NULL);
    return m_elements[index];
}

template <class ElemT, class ArrayT>
ElemT& JArrayElements<ElemT, ArrayT>::operator[](jsize index)
{
    assert(m_elements != NULL);
    return m_elements[index];
}

template <>
inline void JArrayElements<jboolean, jbooleanArray>::commit()
{
    assert(m_elements != NULL);
    JNIEnv* penv = m_array.getEnv();
    penv->ReleaseBooleanArrayElements(m_array, m_elements, JNI_COMMIT);
}

template <>
inline void JArrayElements<jboolean, jbooleanArray>::release(bool commit)
{
    assert(m_elements != NULL);
    JNIEnv* penv = m_array.getEnv();
    penv->ReleaseBooleanArrayElements(m_array, m_elements, commit ? 0 : JNI_ABORT);
    m_elements = NULL;
}

template <>
inline jboolean* JArrayElements<jboolean, jbooleanArray>::getElements(
    JNIEnv* penv, jbooleanArray array)
{
    jboolean* elements = penv->GetBooleanArrayElements(array, NULL);
    if (elements == NULL) {
        convertException(penv, "Unable to access primitive array elements");
    }
    return elements;
}

template <>
inline void JArrayElements<jbyte, jbyteArray>::commit()
{
    assert(m_elements != NULL);
    JNIEnv* penv = m_array.getEnv();
    penv->ReleaseByteArrayElements(m_array, m_elements, JNI_COMMIT);
}

template <>
inline void JArrayElements<jbyte, jbyteArray>::release(bool commit)
{
    assert(m_elements != NULL);
    JNIEnv* penv = m_array.getEnv();
    penv->ReleaseByteArrayElements(m_array, m_elements, commit ? 0 : JNI_ABORT);
    m_elements = NULL;
}

template <>
inline jbyte* JArrayElements<jbyte, jbyteArray>::getElements(
    JNIEnv* penv, jbyteArray array)
{
    jbyte* elements = penv->GetByteArrayElements(array, NULL);
    if (elements == NULL) {
        convertException(penv, "Unable to access primitive array elements");
    }
    return elements;
}

template <>
inline void JArrayElements<jchar, jcharArray>::commit()
{
    assert(m_elements != NULL);
    JNIEnv* penv = m_array.getEnv();
    penv->ReleaseCharArrayElements(m_array, m_elements, JNI_COMMIT);
}

template <>
inline void JArrayElements<jchar, jcharArray>::release(bool commit)
{
    assert(m_elements != NULL);
    JNIEnv* penv = m_array.getEnv();
    penv->ReleaseCharArrayElements(m_array, m_elements, commit ? 0 : JNI_ABORT);
    m_elements = NULL;
}

template <>
inline jchar* JArrayElements<jchar, jcharArray>::getElements(
    JNIEnv* penv, jcharArray array)
{
    jchar* elements = penv->GetCharArrayElements(array, NULL);
    if (elements == NULL) {
        convertException(penv, "Unable to access primitive array elements");
    }
    return elements;
}

template <>
inline void JArrayElements<jshort, jshortArray>::commit()
{
    assert(m_elements != NULL);
    JNIEnv* penv = m_array.getEnv();
    penv->ReleaseShortArrayElements(m_array, m_elements, JNI_COMMIT);
}

template <>
inline void JArrayElements<jshort, jshortArray>::release(bool commit)
{
    assert(m_elements != NULL);
    JNIEnv* penv = m_array.getEnv();
    penv->ReleaseShortArrayElements(m_array, m_elements, commit ? 0 : JNI_ABORT);
    m_elements = NULL;
}

template <>
inline jshort* JArrayElements<jshort, jshortArray>::getElements(
    JNIEnv* penv, jshortArray array)
{
    jshort* elements = penv->GetShortArrayElements(array, NULL);
    if (elements == NULL) {
        convertException(penv, "Unable to access primitive array elements");
    }
    return elements;
}

template <>
inline void JArrayElements<jint, jintArray>::commit()
{
    assert(m_elements != NULL);
    JNIEnv* penv = m_array.getEnv();
    penv->ReleaseIntArrayElements(m_array, m_elements, JNI_COMMIT);
}

template <>
inline void JArrayElements<jint, jintArray>::release(bool commit)
{
    assert(m_elements != NULL);
    JNIEnv* penv = m_array.getEnv();
    penv->ReleaseIntArrayElements(m_array, m_elements, commit ? 0 : JNI_ABORT);
    m_elements = NULL;
}

template <>
inline jint* JArrayElements<jint, jintArray>::getElements(
    JNIEnv* penv, jintArray array)
{
    jint* elements = penv->GetIntArrayElements(array, NULL);
    if (elements == NULL) {
        convertException(penv, "Unable to access primitive array elements");
    }
    return elements;
}

template <>
inline void JArrayElements<jlong, jlongArray>::commit()
{
    assert(m_elements != NULL);
    JNIEnv* penv = m_array.getEnv();
    penv->ReleaseLongArrayElements(m_array, m_elements, JNI_COMMIT);
}

template <>
inline void JArrayElements<jlong, jlongArray>::release(bool commit)
{
    assert(m_elements != NULL);
    JNIEnv* penv = m_array.getEnv();
    penv->ReleaseLongArrayElements(m_array, m_elements, commit ? 0 : JNI_ABORT);
    m_elements = NULL;
}

template <>
inline jlong* JArrayElements<jlong, jlongArray>::getElements(
    JNIEnv* penv, jlongArray array)
{
    jlong* elements = penv->GetLongArrayElements(array, NULL);
    if (elements == NULL) {
        convertException(penv, "Unable to access primitive array elements");
    }
    return elements;
}

template <>
inline void JArrayElements<jfloat, jfloatArray>::commit()
{
    assert(m_elements != NULL);
    JNIEnv* penv = m_array.getEnv();
    penv->ReleaseFloatArrayElements(m_array, m_elements, JNI_COMMIT);
}

template <>
inline void JArrayElements<jfloat, jfloatArray>::release(bool commit)
{
    assert(m_elements != NULL);
    JNIEnv* penv = m_array.getEnv();
    penv->ReleaseFloatArrayElements(m_array, m_elements, commit ? 0 : JNI_ABORT);
    m_elements = NULL;
}

template <>
inline jfloat* JArrayElements<jfloat, jfloatArray>::getElements(
    JNIEnv* penv, jfloatArray array)
{
    jfloat* elements = penv->GetFloatArrayElements(array, NULL);
    if (elements == NULL) {
        convertException(penv, "Unable to access primitive array elements");
    }
    return elements;
}

template <>
inline void JArrayElements<jdouble, jdoubleArray>::commit()
{
    assert(m_elements != NULL);
    JNIEnv* penv = m_array.getEnv();
    penv->ReleaseDoubleArrayElements(m_array, m_elements, JNI_COMMIT);
}

template <>
inline void JArrayElements<jdouble, jdoubleArray>::release(bool commit)
{
    assert(m_elements != NULL);
    JNIEnv* penv = m_array.getEnv();
    penv->ReleaseDoubleArrayElements(m_array, m_elements, commit ? 0 : JNI_ABORT);
    m_elements = NULL;
}

template <>
inline jdouble* JArrayElements<jdouble, jdoubleArray>::getElements(
    JNIEnv* penv, jdoubleArray array)
{
    jdouble* elements = penv->GetDoubleArrayElements(array, NULL);
    if (elements == NULL) {
        convertException(penv, "Unable to access primitive array elements");
    }
    return elements;
}

//////////////////////////////////////////////////////////////////////

template <class ElemT>
JObjectArrayTmpl<ElemT>::JObjectArrayTmpl(JNIEnv* penv, jobjectArray obj, bool freeLocal) :
    base_type(penv, obj, freeLocal)
{
    // done
}

template <class ElemT>
JObjectArrayTmpl<ElemT>::JObjectArrayTmpl(JNIEnv* penv, jsize length, jclass elemCls, jobject initElem) :
    base_type(penv, newArray(penv, length, elemCls, initElem), true)
{
    // done
}

template <class ElemT>
const JObjectArrayElementTmpl<ElemT> JObjectArrayTmpl<ElemT>::operator[](jsize index) const
{
    return JObjectArrayElementTmpl<ElemT>(*this, index);
}

template <class ElemT>
JObjectArrayElementTmpl<ElemT> JObjectArrayTmpl<ElemT>::operator[](jsize index)
{
    return JObjectArrayElementTmpl<ElemT>(*this, index);
}

template <class ElemT>
jobjectArray JObjectArrayTmpl<ElemT>::newArray(JNIEnv* penv, jsize length, jclass elemCls,
    jobject initElem)
{
    jobjectArray ref = penv->NewObjectArray(length, elemCls, initElem);
#ifdef JNICPP_DEBUG_REF
    printf("NewObjectArray: penv=%p, length=%d, elemCls=%p, initElem=%p, ref=%p\n",
        penv, length, elemCls, initElem, ref);
#endif
    if (ref == NULL) {
        convertException(penv, "Unable to create new array");
    }
    return ref;
}

//////////////////////////////////////////////////////////////////////

template <class ElemT>
JObjectArrayElementTmpl<ElemT>::JObjectArrayElementTmpl(
    const JObjectArrayTmpl<ElemT>& array, jsize index) :
    m_array(array),
    m_index(index)
{
    // done
}

template <class ElemT>
JObjectArrayElementTmpl<ElemT>::operator ElemT() const
{
    JNIEnv* penv = m_array.getEnv();
    ElemT result = static_cast<jobject>(
        penv->GetObjectArrayElement(m_array, m_index));
    checkException(penv);
    return result;
}

template <class ElemT>
JObjectArrayElementTmpl<ElemT>& JObjectArrayElementTmpl<ElemT>::operator=(const ElemT& value)
{
    JNIEnv* penv = m_array.getEnv();
    penv->SetObjectArrayElement(m_array, m_index, value);
    checkException(penv);
    return *this;
}

//////////////////////////////////////////////////////////////////////

inline JThrowable::JThrowable(JNIEnv* penv, jthrowable obj, bool freeLocal) :
    base_type(penv, obj, freeLocal)
{
    // done
}

//////////////////////////////////////////////////////////////////////

inline JException::JException(const std::string& msg) throw() :
    m_msg(msg)
{
    // done
}

inline JException::JException(const JException& rhs) throw() :
    m_msg(rhs.m_msg)
{
    // done
}

inline JException::~JException() throw()
{
    // done
}

inline const char* JException::what() const throw()
{
    return m_msg.c_str();
}

//////////////////////////////////////////////////////////////////////

inline JVMException::JVMException(JNIEnv* penv, jthrowable obj) throw() :
    JException(getMessage(penv, obj)),
    m_throwable(penv, obj, true)
{
    // done
}

inline JVMException::JVMException(const JVMException& rhs) throw() :
    JException(rhs),
    m_throwable(rhs.m_throwable)
{
    // done
}

inline JVMException::~JVMException() throw()
{
    // done
}

inline JThrowable JVMException::getThrowable() const throw()
{
    return m_throwable;
}

//////////////////////////////////////////////////////////////////////

inline JEnv::JEnv(JNIEnv* penv) throw() :
    m_penv(penv)
{
    // done
}

inline JEnv::JEnv(JavaVM* pvm) :
    m_penv(attachThread(pvm))
{
    // done
}

inline JEnv::operator JNIEnv*() const throw()
{
    return m_penv;
}

inline JavaVM* JEnv::getVM() const
{
    JavaVM* pvm;
    if (m_penv->GetJavaVM(&pvm) != JNI_OK) {
        throw JException("Unable to determine JVM for current thread");
    }
    return pvm;
}

inline int JEnv::getVersion() const throw()
{
    return m_penv->GetVersion();
}

inline JClass JEnv::findClass(const char* name) const
{
    return JClass(m_penv, name);
}

inline jclass JEnv::getClass(jobject obj) const throw()
{
    return m_penv->GetObjectClass(obj);
}

inline bool JEnv::isInstanceOf(jobject obj, jclass cls) const throw()
{
    return m_penv->IsInstanceOf(obj, cls);
}

inline bool JEnv::isSameObject(jobject obj, jobject that) const throw()
{
    return m_penv->IsSameObject(obj, that);
}

inline JString JEnv::newString(const char* str) const
{
    return JString(m_penv, str);
}

inline void JEnv::throwException(jthrowable obj) const
{
    if (m_penv->Throw(obj) != JNI_OK) {
        throw JException("Unable to throw Java exception");
    }
}

inline void JEnv::detachThread()
{
    JavaVM* pvm = getVM();
    if (pvm->DetachCurrentThread() != JNI_OK) {
        throw JException("Unable to detach thread from JVM");
    }
    m_penv = NULL;
}

inline JNIEnv* JEnv::attachThread(JavaVM* pvm)
{
    JNIEnv* penv;
    if (pvm->AttachCurrentThread(reinterpret_cast<void**>(&penv), NULL) != JNI_OK) {
        throw JException("Unable to attach thread to JVM");
    }
    return penv;
}

//////////////////////////////////////////////////////////////////////

}
