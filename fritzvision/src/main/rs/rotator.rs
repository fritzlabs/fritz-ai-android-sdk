// Taken from: https://github.com/riwnodennyk/ImageRotation/tree/master/app/src/main
#pragma version(1)
#pragma rs java_package_name(ai.fritz.vision.rs)

rs_allocation inImage;
int inWidth;
int inHeight;

uchar4 __attribute__ ((kernel)) rotate_270_clockwise (uchar4 in, uint32_t x, uint32_t y) {
    uint32_t inX  = inWidth - 1 - y;
    uint32_t inY = x;
    const uchar4 *out = rsGetElementAt(inImage, inX, inY);
    return *out;
}

uchar4 __attribute__ ((kernel)) rotate_90_clockwise (uchar4 in, uint32_t x, uint32_t y) {
    uint32_t inX = y;
    uint32_t inY = inHeight - 1 - x;

    const uchar4 *out = rsGetElementAt(inImage, inX, inY);
    return *out;
}

uchar4 __attribute__ ((kernel)) rotate_180 (uchar4 in, uint32_t x, uint32_t y) {
    uint32_t inX = inWidth - 1 - x;
    uint32_t inY = inHeight - 1 - y;

    const uchar4 *out = rsGetElementAt(inImage, inX, inY);
    return *out;
}

uchar4 __attribute__ ((kernel)) flip_vertical (uchar4 in, uint32_t x, uint32_t y) {
    uint32_t inX = x;
    uint32_t inY = inHeight - 1 - y;

    const uchar4 *out = rsGetElementAt(inImage, inX, inY);
    return *out;
}

uchar4 __attribute__ ((kernel)) flip_horizontal (uchar4 in, uint32_t x, uint32_t y) {
    uint32_t inX = inWidth - 1 - x;
    uint32_t inY = y;

    const uchar4 *out = rsGetElementAt(inImage, inX, inY);
    return *out;
}