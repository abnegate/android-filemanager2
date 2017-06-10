//
// Created by Jake on 6/9/2017.
//
#include <jni.h>
#include <stdio.h>

JNIEXPORT jint JNICALL
              Java_com_jakebarnby_filemanager_services_SourceTransferService_copyFileNative(JNIEnv *env,
                                                                                      jobject instance,
                                                                                      jstring sourcePath_,
                                                                                      jstring destinationPath_)
{
    const char *sourcePath = (*env)->GetStringUTFChars(env, sourcePath_, 0);
    const char *destinationPath = (*env)->GetStringUTFChars(env, destinationPath_, 0);

    FILE *from, *to;
    char ch;

    if (sourcePath == destinationPath) {
        return 0;
    }

    // Open source file
    if((from = fopen(sourcePath, "rb"))==NULL) {
        printf("Cannot open source file.\n");
    }

    //Open destination file
    if((to = fopen(destinationPath, "wb"))==NULL) {
        printf("Cannot open destination file.\n");
        return -1;
    }

    // Copy the file
    while(!feof(from)) {
        ch = fgetc(from);
        if(ferror(from)) {
            printf("Error reading source file.\n");
            return -1;
        }
        if(!feof(from)) fputc(ch, to);
        if(ferror(to)) {
            printf("Error writing destination file.\n");
            return -1;
        }
    }

    if(fclose(from)==EOF) {
        printf("Error closing source file.\n");
        return -1;
    }

    if(fclose(to)==EOF) {
        printf("Error closing destination file.\n");
        return -1;
    }

    (*env)->ReleaseStringUTFChars(env, sourcePath_, sourcePath);
    (*env)->ReleaseStringUTFChars(env, destinationPath_, destinationPath);
    return 0;
}

JNIEXPORT jint JNICALL
Java_com_jakebarnby_filemanager_services_SourceTransferService_moveFileNative(JNIEnv *env,
                                                                         jobject instance,
                                                                         jstring sourcePath_,
                                                                         jstring destinationPath_) {
    const char *sourcePath = (*env)->GetStringUTFChars(env, sourcePath_, 0);
    const char *destinationPath = (*env)->GetStringUTFChars(env, destinationPath_, 0);

    // TODO

    (*env)->ReleaseStringUTFChars(env, sourcePath_, sourcePath);
    (*env)->ReleaseStringUTFChars(env, destinationPath_, destinationPath);
}

JNIEXPORT jint JNICALL
Java_com_jakebarnby_filemanager_services_SourceTransferService_deleteFileNative(JNIEnv *env,
                                                                          jobject instance,
                                                                          jstring sourcePath_) {
    const char *sourcePath = (*env)->GetStringUTFChars(env, sourcePath_, 0);

    remove(sourcePath);

    (*env)->ReleaseStringUTFChars(env, sourcePath_, sourcePath);
}