//package com.toolbox.io.text;
//
//import com.toolbox.io.buffer.BufferedOutput;
//import com.toolbox.io.text.internal.ASCIITextOutput;
//import com.toolbox.io.text.internal.GenericTextOutput;
//import com.toolbox.io.text.internal.Latin1TextOutput;
//import com.toolbox.io.text.internal.UTF8TextOutput;
//import com.toolbox.io.text.output.TextOutputProvider;
//
//import java.nio.charset.Charset;
//import java.nio.charset.StandardCharsets;
//import java.util.ServiceLoader;
//
//public final class TextOutputFactory {
//
//    private static final TextOutputProvider PROVIDER = loadProvider();
//
//    private static TextOutputProvider loadProvider() {
//        ServiceLoader<TextOutputProvider> loader = ServiceLoader.load(TextOutputProvider.class);
//        TextOutputProvider best = null;
//        int bestPriority = -1;
//        for (TextOutputProvider provider : loader) {
//            if (provider.isAvailable() && provider.priority() > bestPriority) {
//                best = provider;
//                bestPriority = provider.priority();
//            }
//        }
//        return best != null ? best : new FallbackProvider();
//    }
//
//    public static TextOutput create(BufferedOutput out, Charset charset, String eol) {
//        if (PROVIDER != null) {
//            TextOutput optimized = PROVIDER.createOptimized(out, charset, eol);
//            if (optimized != null) return optimized;
//        }
//        return createBuiltIn(out, charset, eol);
//    }
//
//    static TextOutput createBuiltIn(BufferedOutput out, Charset charset, String eol) {
//        if (charset == StandardCharsets.ISO_8859_1) {
//            return new Latin1TextOutput(out, eol.getBytes(charset));
//        } else if (charset == StandardCharsets.UTF_8) {
//            return new UTF8TextOutput(out, eol.getBytes(charset));
//        } else if (charset == StandardCharsets.US_ASCII) {
//            return new ASCIITextOutput(out, eol.getBytes(charset));
//        }
//        return new GenericTextOutput(out, charset, eol);
//    }
//
//    private static class FallbackProvider implements TextOutputProvider {
//        public boolean isAvailable() {return true;}
//        public int priority() {return 0;}
//        public TextOutput createOptimized(BufferedOutput out, Charset charset, String eol) {return null;}
//    }
//}