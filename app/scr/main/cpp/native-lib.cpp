#include <jni.h>
#include <android/log.h>
#include <cstdint>
#include <vector>

#define LOG_TAG "EspCore"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jintArray JNICALL
Java_com_axion_menuoverlay_EspEngine_nativeDetectRedBlobs(
        JNIEnv* env, jobject /*this*/,
        jintArray pixels_, jint width, jint height,
        jint minBlobPx, jint maxBlobPx,
        jint hMin, jint hMax, jint sMin, jint vMin) {

    jsize len = env->GetArrayLength(pixels_);
    if (len < width * height) return env->NewIntArray(0);

    jint* pixels = env->GetIntArrayElements(pixels_, nullptr);
    if (!pixels) return env->NewIntArray(0);

    std::vector<uint8_t> mask((size_t)width * height, 0);
    for (int y = 0; y < height; ++y) {
        for (int x = 0; x < width; ++x) {
            uint32_t p = (uint32_t)pixels[y * width + x];
            int r = (p >> 16) & 0xFF;
            int g = (p >> 8) & 0xFF;
            int b = p & 0xFF;

            int mx = r > g ? (r > b ? r : b) : (g > b ? g : b);
            int mn = r < g ? (r < b ? r : b) : (g < b ? g : b);
            int d = mx - mn;

            if (mx < vMin || d == 0) continue;
            int s = d * 255 / mx;
            if (s < sMin) continue;

            int h;
            if (mx == r) h = 60 * (g - b) / d;
            else if (mx == g) h = 120 + 60 * (b - r) / d;
            else h = 240 + 60 * (r - g) / d;
            if (h < 0) h += 360;

            bool isRed = (h >= hMin && h <= hMax) || (h >= 360 + hMin && h <= 360 + hMax);
            if (isRed) mask[(size_t)y * width + x] = 1;
        }
    }

    env->ReleaseIntArrayElements(pixels_, pixels, JNI_ABORT);

    std::vector<int> out;
    std::vector<uint8_t> visited((size_t)width * height, 0);
    std::vector<int> stack;
    stack.reserve(1024);

    for (int y0 = 0; y0 < height; ++y0) {
        for (int x0 = 0; x0 < width; ++x0) {
            size_t idx0 = (size_t)y0 * width + x0;
            if (!mask[idx0] || visited[idx0]) continue;

            int minX = x0, maxX = x0, minY = y0, maxY = y0;
            int count = 0;

            stack.clear();
            stack.push_back(y0 * width + x0);
            visited[idx0] = 1;

            while (!stack.empty()) {
                int cur = stack.back(); stack.pop_back();
                int cx = cur % width;
                int cy = cur / width;
                count++;
                if (cx < minX) minX = cx;
                if (cx > maxX) maxX = cx;
                if (cy < minY) minY = cy;
                if (cy > maxY) maxY = cy;

                const int dx[4] = {1, -1, 0, 0};
                const int dy[4] = {0, 0, 1, -1};
                for (int k = 0; k < 4; ++k) {
                    int nx = cx + dx[k];
                    int ny = cy + dy[k];
                    if (nx < 0 || ny < 0 || nx >= width || ny >= height) continue;
                    size_t ni = (size_t)ny * width + nx;
                    if (mask[ni] && !visited[ni]) {
                        visited[ni] = 1;
                        stack.push_back(ny * width + nx);
                    }
                }
            }

            if (count >= minBlobPx && count <= maxBlobPx) {
                int w = maxX - minX + 1;
                int h = maxY - minY + 1;
                float ratio = (float)w / (float)h;
                if (ratio < 0.4f || ratio > 2.5f) continue;
                int cx = (minX + maxX) / 2;
                int cy = (minY + maxY) / 2;
                out.push_back(cx);
                out.push_back(cy);
                out.push_back(w);
                out.push_back(h);
            }
        }
    }

    jintArray result = env->NewIntArray((jsize)out.size());
    if (!out.empty()) env->SetIntArrayRegion(result, 0, (jsize)out.size(), out.data());
    return result;
}
