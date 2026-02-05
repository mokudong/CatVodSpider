package com.github.catvod;

import com.github.catvod.net.OkHttpTest;
import com.github.catvod.utils.CryptoTest;
import com.github.catvod.utils.JsonValidatorTest;
import com.github.catvod.utils.PathTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * 所有单元测试的测试套件
 * <p>
 * 运行此类可以执行所有单元测试。
 * </p>
 * <p>
 * <b>运行方式：</b>
 * <pre>
 * # 命令行
 * ./gradlew test
 *
 * # Android Studio
 * 右键点击此文件 → Run 'AllTests'
 * </pre>
 * </p>
 *
 * @author CatVod Team
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        JsonValidatorTest.class,
        CryptoTest.class,
        OkHttpTest.class,
        PathTest.class
})
public class AllTests {
    // 测试套件入口，不需要代码
}
