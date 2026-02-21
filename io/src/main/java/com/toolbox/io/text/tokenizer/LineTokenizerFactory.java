//package com.toolbox.io.text;
//
//import com.toolbox.io.buffer.BufferedInput;
//import com.toolbox.io.text.spi.LineTokenizerProvider;
//
//import java.io.IOException;
//import java.nio.charset.Charset;
//import java.util.ServiceLoader;
//
//public final class LineTokenizerFactory {
//
//    private static final LineTokenizerProvider PROVIDER = loadProvider();
//
//    private static LineTokenizerProvider loadProvider() {
//        ServiceLoader<LineTokenizerProvider> loader = ServiceLoader.load(LineTokenizerProvider.class);
//        LineTokenizerProvider best = null;
//        int bestPriority = -1;
//        for (LineTokenizerProvider provider : loader) {
//            if (provider.isAvailable() && provider.priority() > bestPriority) {
//                best = provider;
//                bestPriority = provider.priority();
//            }
//        }
//        return best != null ? best : new FallbackProvider();
//    }
//
//    public static LineTokenizer create(BufferedInput in, Charset charset, byte eol, byte preEol) throws IOException {
//        if (PROVIDER != null) {
//            LineTokenizer optimized = PROVIDER.createTokenizer(in, charset, eol, preEol);
//            if (optimized != null) return optimized;
//        }
//        return createBuiltIn(in, charset, eol, preEol);
//    }
//
//    static LineTokenizer createBuiltIn(BufferedInput in, Charset charset, byte eol, byte preEol) throws IOException {
//        return HeapScalarLineTokenizer.of(in, charset, eol, preEol);
//    }
//
//    private static class FallbackProvider implements LineTokenizerProvider {
//        public boolean isAvailable() {return true;}
//        public int priority() {return 0;}
//        public LineTokenizer createTokenizer(BufferedInput in, Charset charset, byte eol, byte preEol) throws IOException {
//            return null;
//        }
//    }
//}