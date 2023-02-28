package com.lollipop.filechooser

sealed class FileMime {

    abstract val suffix: String
    abstract val value: String

    class Custom(override val value: String, override val suffix: String = "") : FileMime()

    object ALL : FileMime() {
        override val suffix: String = ""
        override val value: String = "*/*"
    }

    sealed class Image(override val suffix: String, override val value: String) : FileMime() {
        object ALL : Image("", "image/*")
        object PNG : Image("png", "image/png")
        object BMP : Image("bmp", "image/bmp")
        object GIF : Image("gif", "image/gif")
        object JPEG : Image("jpeg", "image/jpeg")
        object JPG : Image("jpg", "image/jpeg")
    }

    sealed class Text(override val suffix: String, override val value: String) : FileMime() {
        object ALL : Text("", "text/*")
        object C : Text("c", "text/plain")
        object CONF : Text("conf", "text/plain")
        object CPP : Text("cpp", "text/plain")
        object H : Text("h", "text/plain")
        object HTM : Text("htm", "text/html")
        object HTML : Text("html", "text/html")
        object JAVA : Text("java", "text/plain")
        object LOG : Text("log", "text/plain")
        object PROP : Text("prop", "text/plain")
        object RC : Text("rc", "text/plain")
        object SH : Text("sh", "text/plain")
        object TXT : Text("txt", "text/plain")
        object XML : Text("xml", "text/plain")
    }

    sealed class Video(override val suffix: String, override val value: String) : FileMime() {
        object ALL : Video("", "video/*")
        object GP3 : Video("3gp", "video/3gpp")
        object ASF : Video("asf", "video/x-ms-asf")
        object AVI : Video("avi", "video/x-msvideo")
        object M4U : Video("m4u", "video/vnd.mpegurl")
        object M4V : Video("m4v", "video/x-m4v")
        object MOV : Video("mov", "video/quicktime")
        object MP4 : Video("mp4", "video/mp4")
        object MPE : Video("mpe", "video/mpeg")
        object MPEG : Video("mpeg", "video/mpeg")
        object MPG : Video("mpg", "video/mpeg")
        object MPG4 : Video("mpg4", "video/mp4")
    }

    sealed class Audio(override val suffix: String, override val value: String) : FileMime() {
        object ALL : Audio("", "audio/*")
        object M3U : Audio("m3u", "audio/x-mpegurl")
        object M4A : Audio("m4a", "audio/mp4a-latm")
        object M4B : Audio("m4b", "audio/mp4a-latm")
        object M4P : Audio("m4p", "audio/mp4a-latm")
        object MP2 : Audio("mp2", "audio/x-mpeg")
        object MP3 : Audio("mp3", "audio/x-mpeg")
        object MPGA : Audio("mpga", "audio/mpeg")
        object OGG : Audio("ogg", "audio/ogg")
        object RMVB : Audio("rmvb", "audio/x-pn-realaudio")
        object WAV : Audio("wav", "audio/x-wav")
        object WMA : Audio("wma", "audio/x-ms-wma")
        object WMV : Audio("wmv", "audio/x-ms-wmv")
    }

    sealed class Application(override val suffix: String, override val value: String) : FileMime() {
        object ALL : Application("", "application/*")
        object APK : Application("apk", "application/vnd.android.package-archive")
        object BIN : Application("bin", "application/octet-stream")
        object CLASS : Application("class", "application/octet-stream")
        object DOC : Application("doc", "application/msword")
        object DOCX : Application(
            "docx",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        )

        object XLS : Application("xls", "application/vnd.ms-excel")
        object XLSX :
            Application("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")

        object EXE : Application("exe", "application/octet-stream")
        object GTAR : Application("gtar", "application/x-gtar")
        object GZ : Application("gz", "application/x-gzip")
        object JAR : Application("jar", "application/java-archive")
        object JS : Application("js", "application/x-javascript")
        object MPC : Application("mpc", "application/vnd.mpohun.certificate")
        object MSG : Application("msg", "application/vnd.ms-outlook")
        object PDF : Application("pdf", "application/pdf")
        object PPS : Application("pps", "application/vnd.ms-powerpoint")
        object PPT : Application("ppt", "application/vnd.ms-powerpoint")
        object PPTX : Application(
            "pptx",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation"
        )

        object RTF : Application("rtf", "application/rtf")
        object TAR : Application("tar", "application/x-tar")
        object TGZ : Application("tgz", "application/x-compressed")
        object WPS : Application("wps", "application/vnd.ms-works")
        object Z : Application("z", "application/x-compress")
        object ZIP : Application("zip", "application/x-zip-compressed")
    }

}