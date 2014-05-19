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

#ifndef jnicpp_h_included
#define jnicpp_h_included

#include <exception>
#include <string>
#include <list>

#ifdef __CYGWIN__
    // include the __int64 def needed by jni.h
    #include <w32api/basetyps.h>
#endif

#include <jni.h>

namespace jnicpp {

//////////////////////////////////////////////////////////////////////

template<class ClassT = jobject>
class JRef
{
public:
    typedef ClassT class_type;

protected:
    JRef(JNIEnv* penv, ClassT obj);

private:
    // copy constructor not supported
    JRef(const JRef<ClassT>& that);

    // assignment operator not supported
    JRef<ClassT>& operator=(const JRef<ClassT>& that);

public:
    // subclasses override destructor to delete the reference
    virtual ~JRef();

    // automatic conversion to contained jobject
    operator ClassT() const throw();

    // obtain contained jobject and set this reference to NULL
    ClassT release() throw();

    JNIEnv* getEnv() const throw();

protected:
    JNIEnv* m_penv;
    ClassT m_obj;
};

//////////////////////////////////////////////////////////////////////

template<class ClassT = jobject>
class JLocalRef : public JRef<ClassT>
{
public:
    JLocalRef(JNIEnv* penv, ClassT obj);

private:
    // copy constructor not supported
    JLocalRef(const JLocalRef<ClassT>& that);

    // assignment operator not supported
    JLocalRef<ClassT>& operator=(const JLocalRef<ClassT>& that);

public:
    ~JLocalRef();

    static ClassT newLocalRef(JNIEnv* penv, ClassT obj);
};

//////////////////////////////////////////////////////////////////////

template<class ClassT = jobject>
class JGlobalRef : public JRef<ClassT>
{
public:
    JGlobalRef(JNIEnv* penv, ClassT obj, bool freeLocal);

    // copy constructor
    JGlobalRef(const JGlobalRef<ClassT>& that);

    // copy constructor for ClassT-derived JRef
    // WORKAROUND: member templates must be defined inline for MSVC 6
    template<class DerivedClassT>
    JGlobalRef(const JRef<DerivedClassT>& that) :
        JRef<ClassT>(that.getEnv(), newGlobalRef(that.getEnv(), that, false))
    {
        // done
    }

    // assignment operator
    JGlobalRef<ClassT>& operator=(const JGlobalRef<ClassT>& that);

    // assignment operator for ClassT-derived JRef
    // WORKAROUND: member templates must be defined inline for MSVC 6
    template<class DerivedClassT>
    JGlobalRef<ClassT>& operator=(const JRef<DerivedClassT>& that)
    {
        if (*this != that) {
            assignRef(that.m_penv, that.m_obj);
        }
        return *this;
    }

    virtual ~JGlobalRef();

    static ClassT newGlobalRef(JNIEnv* penv, ClassT obj, bool freeLocal);

private:
    void assignRef(JNIEnv* penv, ClassT obj);
};

//////////////////////////////////////////////////////////////////////

template<class ClassT = jobject>
class JObjectTmpl : public JGlobalRef<ClassT>
{
    typedef JGlobalRef<ClassT> base_type;

public:
    JObjectTmpl(JNIEnv* penv, ClassT obj, bool freeLocal);

#ifndef _MSC_VER
    // copy constructor
    // WORKAROUND: MSVC 6 seems to treat the template constructor below
    // as the copy constructor
    JObjectTmpl(const JObjectTmpl<ClassT>& that);
#endif

    // copy constructor for ClassT-derived JObjectTmpl
    // WORKAROUND: member templates must be defined inline for MSVC 6
    template<class DerivedClassT>
    JObjectTmpl(const JObjectTmpl<DerivedClassT>& that) :
        base_type(that)
    {
        // done
    }

    jclass getClass() const throw();

    bool isInstanceOf(jclass cls) const throw();

    bool isSameObject(jobject that) const throw();

private:
    // assignment operator not supported
    JObjectTmpl<ClassT>& operator=(const JObjectTmpl<ClassT>& that);
};

typedef JObjectTmpl<> JObject;

//////////////////////////////////////////////////////////////////////

template <class ReturnT> class JMethod;
template <class ReturnT> class JStaticMethod;
template <class ClassT> class JCtorTmpl;
template <class FieldT> class JField;
template <class FieldT> class JStaticField;

class JClass : public JObjectTmpl<jclass>
{
    typedef JObjectTmpl<jclass> base_type;

public:
    JClass(JNIEnv* penv, jclass obj, bool freeLocal);
    JClass(JNIEnv* penv, const char* name);

    // NOTE: requires compiler support for explicit function template instantiation;
    // use JMethod constructor directly if not available
    template <class ReturnT>
    JMethod<ReturnT> getMethod(const char* name, const char* signature) const;

    // NOTE: requires compiler support for explicit function template instantiation;
    // use JStaticMethod constructor directly if not available
    template <class ReturnT>
    JStaticMethod<ReturnT> getStaticMethod(const char* name, const char* signature) const;

    // NOTE: requires compiler support for explicit function template instantiation;
    // use JCtorTmpl constructor directly if not available
    template <class ClassT>
    JCtorTmpl<ClassT> getCtorTmpl(const char* signature) const;

    // NOTE: requires compiler support for explicit function template instantiation;
    // use JField constructor directly if not available
    template <class FieldT>
    JField<FieldT> getField(const char* name, const char* signature) const;

    // NOTE: requires compiler support for explicit function template instantiation;
    // use JStaticField constructor directly if not available
    template <class FieldT>
    JStaticField<FieldT> getStaticField(const char* name, const char* signature) const;

    void registerNativeMethod(const char* name, const char* signature, void* fnptr);
    void unregisterNativeMethods();

    // returns a local reference to a jclass
    static jclass findClass(JNIEnv* penv, const char* name);
};

//////////////////////////////////////////////////////////////////////

template <int MaxArgs = 16>
class JArgumentsN
{
public:
    JArgumentsN();

    JArgumentsN<MaxArgs>& operator<<(jboolean z) throw();
    JArgumentsN<MaxArgs>& operator<<(jbyte b) throw();
    JArgumentsN<MaxArgs>& operator<<(jchar c) throw();
    JArgumentsN<MaxArgs>& operator<<(jshort s) throw();
    JArgumentsN<MaxArgs>& operator<<(jint i) throw();
    JArgumentsN<MaxArgs>& operator<<(jlong j) throw();
    JArgumentsN<MaxArgs>& operator<<(jfloat f) throw();
    JArgumentsN<MaxArgs>& operator<<(jdouble d) throw();
    JArgumentsN<MaxArgs>& operator<<(jobject l) throw();

    operator jvalue*() throw();

private:
    jvalue m_values[MaxArgs];
    int m_cur;
};

typedef JArgumentsN<> JArguments;

//////////////////////////////////////////////////////////////////////

class JAbstractMethod
{
protected:
    JAbstractMethod(const JClass& cls, jmethodID methodID);

public:
    operator jmethodID() const throw();

    static jmethodID getMethodID(JNIEnv* penv, jclass cls,
        const char* name, const char* signature);
    static jmethodID getStaticMethodID(JNIEnv* penv, jclass cls,
        const char* name, const char* signature);

protected:
    JClass m_cls;
    jmethodID m_methodID;
};

//////////////////////////////////////////////////////////////////////

template <class ReturnT>
class JMethod : public JAbstractMethod
{
public:
    JMethod(const JClass& cls, jmethodID methodID);
    JMethod(const JClass& cls, const char* name, const char* signature);

    ReturnT call(jobject obj, jvalue* args = NULL) const;

    // returns a local reference for JMethod<jobject>
    static ReturnT callMethod(JNIEnv* penv, jobject obj, jmethodID methodID,
        jvalue* args = NULL);
};

//////////////////////////////////////////////////////////////////////

template <class ReturnT>
class JStaticMethod : public JAbstractMethod
{
public:
    JStaticMethod(const JClass& cls, jmethodID methodID);
    JStaticMethod(const JClass& cls, const char* name, const char* signature);

    ReturnT call(jvalue* args = NULL) const;

    // returns a local reference for JStaticMethod<jobject>
    static ReturnT callStaticMethod(JNIEnv* penv, jclass cls, jmethodID methodID,
        jvalue* args = NULL);
};

//////////////////////////////////////////////////////////////////////

template<class ClassT = jobject>
class JCtorTmpl : public JAbstractMethod
{
public:
    JCtorTmpl(const JClass& cls, jmethodID methodID);
    JCtorTmpl(const JClass& cls, const char* signature);

    ClassT create(jvalue* args = NULL) const;
    JObjectTmpl<ClassT> createObject(jvalue* args = NULL) const;

    // returns a local reference to a jobject
    static ClassT newObject(JNIEnv* penv, jclass cls, jmethodID methodID,
        jvalue* args = NULL);
};

typedef JCtorTmpl<jobject> JCtor;

//////////////////////////////////////////////////////////////////////

class JAbstractField
{
protected:
    JAbstractField(const JClass& cls, jfieldID fieldID);

public:
    operator jfieldID() const throw();

    static jfieldID getFieldID(JNIEnv* penv, jclass cls,
        const char* name, const char* signature);
    static jfieldID getStaticFieldID(JNIEnv* penv, jclass cls,
        const char* name, const char* signature);

protected:
    JClass m_cls;
    jfieldID m_fieldID;
};

//////////////////////////////////////////////////////////////////////

template <class FieldT>
class JField : public JAbstractField
{
public:
    JField(const JClass& cls, jfieldID fieldID);
    JField(const JClass& cls, const char* name, const char* signature);

    FieldT value(jobject obj) const;

    // returns a local reference for JField<jobject>
    static FieldT getField(JNIEnv* penv, jobject obj, jfieldID fieldID);
};

//////////////////////////////////////////////////////////////////////

template <class FieldT>
class JStaticField : public JAbstractField
{
public:
    JStaticField(const JClass& cls, jfieldID fieldID);
    JStaticField(const JClass& cls, const char* name, const char* signature);

    FieldT value() const;

    // returns a local reference for JStaticField<jobject>
    static FieldT getStaticField(JNIEnv* penv, jclass cls, jfieldID fieldID);
};

//////////////////////////////////////////////////////////////////////

class JString : public JObjectTmpl<jstring>
{
    typedef JObjectTmpl<jstring> base_type;

public:
    JString(JNIEnv* penv, jstring obj, bool freeLocal);
    JString(JNIEnv* penv, const char* str);

    std::string toString() const;

    // returns an array allocated with strdup(); caller must call free()
    char* toCharArray() const;

    // returns a local reference to a jstring
    static jstring newString(JNIEnv* penv, const char* str);

    static std::string convertToString(JNIEnv* penv, jstring obj);

    // returns an array allocated with strdup(); caller must call free()
    static char* convertToCharArray(JNIEnv* penv, jstring obj);

private:
    static jstring newStringNonNull(JNIEnv* penv, const char* str);
};

//////////////////////////////////////////////////////////////////////

template <class ElemT, class ArrayT> class JArrayElements;

template <class ElemT, class ArrayT>
class JArray : public JObjectTmpl<ArrayT>
{
    typedef JObjectTmpl<ArrayT> base_type;

public:
    typedef JArrayElements<ElemT, ArrayT> elements_type;

    JArray(JNIEnv* penv, ArrayT obj, bool freeLocal);
    JArray(JNIEnv* penv, jsize length);

    jsize length();

    // returns a local reference to a jarray
    static ArrayT newArray(JNIEnv* penv, jsize length);
};

typedef JArray<jboolean, jbooleanArray> JBooleanArray;
typedef JArray<jbyte, jbyteArray> JByteArray;
typedef JArray<jchar, jcharArray> JCharArray;
typedef JArray<jshort, jshortArray> JShortArray;
typedef JArray<jint, jintArray> JIntArray;
typedef JArray<jlong, jlongArray> JLongArray;
typedef JArray<jfloat, jfloatArray> JFloatArray;
typedef JArray<jdouble, jdoubleArray> JDoubleArray;

//////////////////////////////////////////////////////////////////////

template <class ElemT, class ArrayT>
class JArrayElements
{
public:
    JArrayElements(const JArray<ElemT, ArrayT>& array);

private:
    // copy constructor not supported
    JArrayElements(const JArrayElements<ElemT, ArrayT>& that);

    // assignment operator not supported
    JArrayElements<ElemT, ArrayT>& operator=(const JArrayElements<ElemT, ArrayT>& that);

public:
    ~JArrayElements();

    operator const ElemT*() const;
    operator ElemT*();

    const ElemT& operator[](jsize index) const;
    ElemT& operator[](jsize index);

    void commit();
    void release(bool commit = true);

private:
    static ElemT* getElements(JNIEnv* penv, ArrayT array);

    JArray<ElemT, ArrayT> m_array;
    ElemT* m_elements;
};

typedef JArrayElements<jboolean, jbooleanArray> JBooleanArrayElements;
typedef JArrayElements<jbyte, jbyteArray> JByteArrayElements;
typedef JArrayElements<jchar, jcharArray> JCharArrayElements;
typedef JArrayElements<jshort, jshortArray> JShortArrayElements;
typedef JArrayElements<jint, jintArray> JIntArrayElements;
typedef JArrayElements<jlong, jlongArray> JLongArrayElements;
typedef JArrayElements<jfloat, jfloatArray> JFloatArrayElements;
typedef JArrayElements<jdouble, jdoubleArray> JDoubleArrayElements;

//////////////////////////////////////////////////////////////////////

template <class ElemT> class JObjectArrayElementTmpl;

template <class ElemT = jobject>
class JObjectArrayTmpl : public JObjectTmpl<jobjectArray>
{
    typedef JObjectTmpl<jobjectArray> base_type;

public:
    typedef JObjectArrayElementTmpl<ElemT> element_type;

    JObjectArrayTmpl(JNIEnv* penv, jobjectArray obj, bool freeLocal);
    JObjectArrayTmpl(JNIEnv* penv, jsize length, jclass elemCls, jobject initElem = NULL);

    const JObjectArrayElementTmpl<ElemT> operator[](jsize index) const;
    JObjectArrayElementTmpl<ElemT> operator[](jsize index);

    // returns a local reference to a jobjectArray
    static jobjectArray newArray(JNIEnv* penv, jsize length, jclass elemCls,
        jobject initElem = NULL);
};

typedef JObjectArrayTmpl<> JObjectArray;

//////////////////////////////////////////////////////////////////////

template <class ElemT = jobject>
class JObjectArrayElementTmpl
{
public:
    JObjectArrayElementTmpl(const JObjectArrayTmpl<ElemT>& array, jsize index);

    // read access
    operator ElemT() const;

    // write access
    JObjectArrayElementTmpl<ElemT>& operator=(const ElemT& value);

private:
    JObjectArrayTmpl<ElemT> m_array;
    jsize m_index;
};

typedef JObjectArrayElementTmpl<> JObjectArrayElement;

//////////////////////////////////////////////////////////////////////

class JThrowable : public JObjectTmpl<jthrowable>
{
    typedef JObjectTmpl<jthrowable> base_type;

public:
    JThrowable(JNIEnv* penv, jthrowable obj, bool freeLocal);
};

//////////////////////////////////////////////////////////////////////

class JException : public std::exception
{
public:
    JException(const std::string& msg) throw();
    JException(const JException& rhs) throw();

    virtual ~JException() throw();

    virtual const char* what() const throw();

private:
    const std::string m_msg;
};

//////////////////////////////////////////////////////////////////////

class JVMException : public JException
{
public:
    JVMException(JNIEnv* penv, jthrowable obj) throw();
    JVMException(const JVMException& rhs) throw();

    virtual ~JVMException() throw();

    JThrowable getThrowable() const throw();

private:
    static std::string getMessage(JNIEnv* penv, jthrowable obj) throw();

    JThrowable m_throwable;
};

//////////////////////////////////////////////////////////////////////

class JEnv
{
public:
    JEnv(JNIEnv* penv) throw();
    JEnv(JavaVM* pvm);

    // automatic conversion to contained JNIEnv*
    operator JNIEnv*() const throw();

    JavaVM* getVM() const;

    int getVersion() const throw();

    JClass findClass(const char* name) const;

    jclass getClass(jobject obj) const throw();

    bool isInstanceOf(jobject obj, jclass cls) const throw();

    bool isSameObject(jobject obj, jobject that) const throw();

    JString newString(const char* str) const;

    void throwException(jthrowable obj) const;

    void detachThread();

private:
    static JNIEnv* attachThread(JavaVM* pvm);

    JNIEnv* m_penv;
};

//////////////////////////////////////////////////////////////////////

class JVMOption
{
public:
    JVMOption(const std::string& optionString, void* extraInfo);

    void initJavaVMOption(JavaVMOption& opt) const;

private:
    std::string m_optionString;
    void* m_extraInfo;
};

//////////////////////////////////////////////////////////////////////

class JVMOptions
{
public:
    void addOption(const std::string& str, void* info = NULL);
    void defineProperty(const std::string& name, const std::string& value);
    void addClassPath(const std::string& path);
    int getOptionCount() const;

    JavaVMOption* newOptionArray() const;
    void deleteOptionArray(JavaVMOption* opts) const;

private:
    typedef std::list<JVMOption> JVMOptionList;
    JVMOptionList m_opts;
};

//////////////////////////////////////////////////////////////////////

class JVM
{
public:
    JVM();
    JVM(const char* path);
    JVM(const JVMOptions& opts);
    JVM(const char* path, const JVMOptions& opts);

    ~JVM();

    // automatic conversion to contained JavaVM*
    operator JavaVM*() const throw();

    static std::string getDefaultJVMPath();

private:
    static JavaVM* createJVM(const char* path, const JVMOptions& opts);

    JavaVM* m_pvm;
};

//////////////////////////////////////////////////////////////////////

}

#include "jnicpp.inl"

#endif // jnicpp_h_included
