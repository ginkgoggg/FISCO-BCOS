package log;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CipherUtils {
    /**
     * AES/CBC/PKCS5Padding 加密
     *
     * @param content    :待加密的内容.
     * @param secret_key :用于生成密钥的 key，自定义即可，加密与解密必须使用同一个，如果不一致，则抛出异常
     * @param vector_key 用于生成算法参数规范的 key，自定义即可，加密与解密必须使用同一个，如果不一致，解密的内容可能会造成与源内容不一致.
     *                   <p>
     *                   1、secret_key、vector_key: AES 时必须是 16 个字节，DES 时必须是 8 字节.
     *                   2、secret_key、vector_key 值不建议使用中文，如果是中文，注意一个汉字是3个字节。
     *                   </p>
     * @return 返回 Cipher 加密后的数据，对加密后的字节数组用 Base64 进行编码转成了可视字符串，如 7giH2bqIMH3kDMIg8gq0nY
     * @throws Exception
     */
    public static String encrypt(String content, String secret_key, String vector_key) throws Exception {
        //实例化 Cipher 对象。使用：AES-高级加密标准算法、CBC-有向量模式、PKCS5Padding-填充方案:（加密内容不足8位时用余位数补足8位）
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        //使用 SecretKeySpec(byte[] key, String algorithm) 创建密钥. 算法要与 Cipher.getInstance 保持一致.
        SecretKey secretKey = new SecretKeySpec(secret_key.getBytes(), "AES");
        /**
         * init(int opMode,Key key,AlgorithmParameterSpec params)：初始化 Cipher，
         * 1、Cipher.ENCRYPT_MODE 表示加密模式
         * 2、key 表示加密密钥
         * 3、params 表示算法参数规范，使用 CBC 有向量模式时，必须传入,如果是 ECB-无向量模式,那么可以不传
         * 4、所有参数规范都必须实现 {@link AlgorithmParameterSpec} 接口.
         */
        IvParameterSpec parameterSpec = new IvParameterSpec(vector_key.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
        /**
         * byte[] doFinal(byte[] content)：对 content 完成加密操作，如果 cipher.init 初始化时使用的解密模式，则此时是解密操作.
         * 返回的是加密后的字节数组，如果直接 new String(byte[] bytes) 是会乱码的，可以借助 BASE64 转为可视字符串，或者转成 16 进制字符
         */
        byte[] encrypted = cipher.doFinal(content.getBytes());
        //BASE64Encoder.encode：BASE64 对字节数组内容进行编码，转为可视字符串，这样方便存储和转换.
        String base64Encode = new BASE64Encoder().encode(encrypted);
        return base64Encode;
    }

    /**
     * AES/CBC/PKCS5Padding 解密
     *
     * @param base64Encode :待解密的内容，因为加密时使用了 Base64 进行了编码，所以这里传入的也是 Base64 编码后的可视化字符串
     * @param secret_key   :用于生成密钥的 key，自定义即可，加密与解密必须使用同一个，如果不一致，则抛出异常
     * @param vector_key   用于生成算法参数规范的 key，自定义即可，加密与解密必须使用同一个，如果不一致，解密的内容可能会造成与源内容不一致.
     *                     <p>
     *                     1、secret_key、vector_key：AES 时必须是 16 个字节，DES 时必须是 8 字节.
     *                     2、secret_key、vector_key 值不建议使用中文，如果是中文，注意一个汉字是3个字节。
     *                     </p>
     * @return
     * @throws Exception
     */
    public static String decrypt(String base64Encode, String secret_key, String vector_key) throws Exception {
        //实例化 Cipher 对象。加密算法/反馈模式/填充方案，解密与加密需要保持一致.
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        //创建密钥。算法也要与实例化 Cipher 一致.
        SecretKey secretKey = new SecretKeySpec(secret_key.getBytes(), "AES");
        //有向量模式(CBC)需要传入 AlgorithmParameterSpec 算法参数规范参数.
        IvParameterSpec parameterSpec = new IvParameterSpec(vector_key.getBytes());
        //初始化 cipher。使用解密模式.
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
        //将 Base64 编码的内容解码成字节数组(因为加密的时候，对密文使用了 Base64编码，所以这里需要先解码)
        byte[] content = new BASE64Decoder().decodeBuffer(base64Encode);
        //执行解密操作。返回解密后的字节数组，此时可以使用 String(byte bytes[]) 转成源字符串.
        byte[] decrypted = cipher.doFinal(content);
        return new String(decrypted);
    }

    public static void main(String[] args) throws Exception {
        String content = "红网时刻5月17日讯（记者 汪衡 通讯员 颜雨彬 汪丹）“场面壮观，气势磅礴，简直就是一部抗洪抢险水域救援大片！”抗洪抢险水域救援实战训练刚一结束，现场围观的群众纷纷发出了感叹。5月16日，湖南省消防救援总队在长沙市望城区千龙湖举办舟艇操作员培训，长沙、株洲、湘潭等40余名消防指战员参加，开展了一场抗洪抢险水域救援实战训练。" +
                "夏以来，随着湖南防汛救援任务的日趋繁重。为提高全省消防救援队伍水域救援能力，充分发挥应急救援“主力军”和“国家队”的作用，湖南省消防救援总队组织了这次为期35天的培训。依托长沙、岳阳、常德3支省级水域救援队设立了3个培训点，全省653名学员分17期参加培训。此次培训采用课堂授课、实地讲解、实操训练等多种形式，培训内容贴近实战，主要包括OSO驾驶、快速出入库、翻船自救、故障排除、200米游泳、受限控艇等项目。省消防救援总队相关负责人介绍，为了加快适应“全灾种、大应急”的职能定位，全面做好“防大汛、抢大险、救大灾”的准备工作，湖南消防救援队伍未雨绸缪，组建了1支省级抗洪抢险救援队、13支市级抗洪抢险救援队，有水域救援任务的消防救援站均成立了1支站级抗洪抢险救援队，总人数2300余人。此外，省消防救援总队还把重型工程机械救援大队、航空救援大队作为抢险救援机动力量纳入调度体系，充分发挥消防救援队伍装备技术优势，最大限度地挽救生命，减少灾害损失。";
        //生成密钥的 key 与 生成算法参数规范的 key 加解密必须一致，它就像是一把钥匙或者口令，不能忘记.
        //AES 加密算法时必须是 16 个字节，DES 时必须是 8 字节.
        String slatKey = "wYDJty8o8HE6YjJS";
        String vectorKey = "SC35fdGrQozSMT5a";
        String encrypt = encrypt(content, slatKey, vectorKey);
        System.out.println("源内容：\n" + content);
        System.out.println("加密后：\n" + encrypt);
        System.out.println("解密后：\n" + decrypt(encrypt, slatKey, vectorKey));
    }
}
