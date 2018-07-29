项目展示：
https://github.com/yzl123456/AR-photo/blob/master/项目效果GIF/ar-photo.gif

AR-Photo为手机app端
zbwz2为app端对应的服务器端
app端采用android studio开发，服务器端用Spring mvc搭建

中文版：
该项目是将博物馆中的文物虚拟化，利用AR增强现实技术与到访的游客进行合影，让文物不再只可远观，带来别样体验。有APP和服务端，APP进行AR合影后将图片上传到服务器，游客通过扫描合影图片右下角的二维码从服务器上下载图片保存。
1.将高通ar的现实情景和JPCT库渲染obj模型整合在一起
2.将模型的变化与用户的手势绑定，可以旋转、平移、缩放
3.拍照后上传图片到服务器端，同时编制了对应URL的二维码，游客可以扫码下载
项目更详细的介绍，可参看我的CSDN博客：https://blog.csdn.net/a376712116/article/details/81267906

English version:
The project is to virtualized the cultural relics in the museum, using AR to enhance the reality technology and the visiting visitors to take a photo, so that the cultural relics can no longer be far view and bring different experiences. the project divide into two parts With APP and server, APP uploads the picture to the server after take a  AR photo, and visitors can download the picture from the server by scanning the two-dimensional code of the lower right corner of the picture picture.
1. integrate the real situation of vuforia AR with the JPCT library rendering obj model.
2. bind the change of the model to the user's gesture,including rotate, translate and zoom.
3. upload photos to the server after taking pictures, and compile a two-dimensional code corresponding to URL. Visitors can scan the code to download.
4. if you want to remove "vuforia" watermark,can mail me or buy key in vofuria website.
more detail about my project,you can view my CSDN blog: https://blog.csdn.net/a376712116/article/details/81267906