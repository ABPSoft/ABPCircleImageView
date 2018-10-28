package com.aminbahrami.abpcircleimageview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;

public class ABPCircleImageView extends android.support.v7.widget.AppCompatImageView
{
	private static final ImageView.ScaleType SCALE_TYPE=ImageView.ScaleType.CENTER_CROP;
	
	private static final Bitmap.Config BITMAP_CONFIG=Bitmap.Config.ARGB_8888;
	private static final int COLOR_DRAWABLE_DIMENSION=2;
	
	private static final int DEFAULT_BORDER_WIDTH=0;
	private static final int DEFAULT_CIRCLE_PADDING=0;
	private static final int DEFAULT_BORDER_COLOR=Color.BLACK;
	private static final int DEFAULT_FILL_COLOR=Color.TRANSPARENT;
	private static final boolean DEFAULT_BORDER_OVERLAY=false;
	
	//Shadow Const
	private static final boolean ENABLE_SHADOW=false;
	private static final int SHADOW_COLOR=Color.BLACK;
	private static final float SHADOW_RADIUS=2f;
	private static final float SHADOW_DX=0f;
	private static final float SHADOW_DY=2f;
	
	private final RectF drawableRect=new RectF();
	private final RectF borderRect=new RectF();
	
	private final Matrix shaderMatrix=new Matrix();
	private Paint bitmapPaint=new Paint();
	private Paint borderPaint=new Paint();
	private Paint fillPaint=new Paint();
	
	private int borderColor=DEFAULT_BORDER_COLOR;
	private int borderWidth=DEFAULT_BORDER_WIDTH;
	private int circlePadding=DEFAULT_CIRCLE_PADDING;
	private int fillColor=DEFAULT_FILL_COLOR;
	
	private Bitmap bitmap;
	private BitmapShader bitmapShader;
	private int bitmapWidth;
	private int bitmapHeight;
	
	private float drawableRadius;
	private float borderRadius;
	
	private ColorFilter colorFilter;
	
	private boolean ready;
	private boolean setupPending;
	private boolean borderOverlay;
	private boolean disableCircularTransformation;
	
	//Shadow
	private boolean enableShadow=ENABLE_SHADOW;
	private int shadowColor=SHADOW_COLOR;
	private float shadowRadius=SHADOW_RADIUS;
	private float shadowDx=SHADOW_DX;
	private float shadowDy=SHADOW_DY;
	
	public ABPCircleImageView(Context context)
	{
		super(context);
		
		init();
	}
	
	public ABPCircleImageView(Context context,AttributeSet attrs)
	{
		this(context,attrs,0);
	}
	
	public ABPCircleImageView(Context context,AttributeSet attrs,int defStyle)
	{
		super(context,attrs,defStyle);
		
		TypedArray a=context.obtainStyledAttributes(attrs,R.styleable.ABPCircleImageView,defStyle,0);
		
		borderWidth=a.getDimensionPixelSize(R.styleable.ABPCircleImageView_borderWidth,DEFAULT_BORDER_WIDTH);
		circlePadding=a.getDimensionPixelSize(R.styleable.ABPCircleImageView_circlePadding,DEFAULT_CIRCLE_PADDING);
		borderColor=a.getColor(R.styleable.ABPCircleImageView_borderColor,DEFAULT_BORDER_COLOR);
		borderOverlay=a.getBoolean(R.styleable.ABPCircleImageView_borderOverlay,DEFAULT_BORDER_OVERLAY);
		fillColor=a.getColor(R.styleable.ABPCircleImageView_fillColor,DEFAULT_FILL_COLOR);
		
		enableShadow=a.getBoolean(R.styleable.ABPCircleImageView_enableShadow,ENABLE_SHADOW);
		shadowColor=a.getColor(R.styleable.ABPCircleImageView_shadowColor,SHADOW_COLOR);
		shadowDx=a.getFloat(R.styleable.ABPCircleImageView_shadowDx,SHADOW_DX);
		shadowDy=a.getFloat(R.styleable.ABPCircleImageView_shadowDx,SHADOW_DY);
		
		a.recycle();
		
		init();
	}
	
	private void init()
	{
		Log.i("ABPCircleImageView","init");
		
		super.setScaleType(SCALE_TYPE);
		
		ready=true;
		
		if(setupPending)
		{
			setup();
			setupPending=false;
		}
	}
	
	@Override
	public ScaleType getScaleType()
	{
		return SCALE_TYPE;
	}
	
	@Override
	public void setScaleType(ScaleType scaleType)
	{
		if(scaleType!=SCALE_TYPE)
		{
			throw new IllegalArgumentException(String.format("ScaleType %s not supported.",scaleType));
		}
	}
	
	@Override
	public void setAdjustViewBounds(boolean adjustViewBounds)
	{
		if(adjustViewBounds)
		{
			throw new IllegalArgumentException("adjustViewBounds not supported.");
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		if(disableCircularTransformation)
		{
			super.onDraw(canvas);
			return;
		}
		
		if(bitmap==null)
		{
			return;
		}
		
		if(fillColor!=Color.TRANSPARENT)
		{
			canvas.drawCircle(drawableRect.centerX(),drawableRect.centerY(),drawableRadius,fillPaint);
		}
		
		canvas.drawCircle(drawableRect.centerX(),drawableRect.centerY(),drawableRadius-circlePadding,bitmapPaint);
		
		if(borderWidth>0)
		{
			canvas.drawCircle(borderRect.centerX(),borderRect.centerY(),borderRadius,borderPaint);
		}
	}
	
	@Override
	protected void onSizeChanged(int w,int h,int oldW,int oldh)
	{
		super.onSizeChanged(w,h,oldW,oldh);
		
		setup();
	}
	
	@Override
	public void setPadding(int left,int top,int right,int bottom)
	{
		super.setPadding(left,top,right,bottom);
		setup();
	}
	
	@Override
	public void setPaddingRelative(int start,int top,int end,int bottom)
	{
		super.setPaddingRelative(start,top,end,bottom);
		setup();
	}
	
	public int getBorderColor()
	{
		return borderColor;
	}
	
	public void setBorderColor(@ColorInt int borderColor)
	{
		if(borderColor==this.borderColor)
		{
			return;
		}
		
		this.borderColor=borderColor;
		borderPaint.setColor(this.borderColor);
		
		invalidate();
	}
	
	/**
	 * @deprecated Use {@link #setBorderColor(int)} instead
	 */
	@Deprecated
	public void setBorderColorResource(@ColorRes int borderColorRes)
	{
		setBorderColor(getContext().getResources().getColor(borderColorRes));
	}
	
	/**
	 * Return the color drawn behind the circle-shaped drawable.
	 *
	 * @return The color drawn behind the drawable
	 * @deprecated Fill color support is going to be removed in the future
	 */
	@Deprecated
	public int getFillColor()
	{
		return fillColor;
	}
	
	/**
	 * Set a color to be drawn behind the circle-shaped drawable. Note that
	 * this has no effect if the drawable is opaque or no drawable is set.
	 *
	 * @param fillColor The color to be drawn behind the drawable
	 * @deprecated Fill color support is going to be removed in the future
	 */
	@Deprecated
	public void setFillColor(@ColorInt int fillColor)
	{
		if(fillColor==this.fillColor)
		{
			return;
		}
		
		this.fillColor=fillColor;
		fillPaint.setColor(fillColor);
		invalidate();
	}
	
	/**
	 * Set a color to be drawn behind the circle-shaped drawable. Note that
	 * this has no effect if the drawable is opaque or no drawable is set.
	 *
	 * @param fillColorRes The color resource to be resolved to a color and
	 *                     drawn behind the drawable
	 * @deprecated Fill color support is going to be removed in the future
	 */
	@Deprecated
	public void setFillColorResource(@ColorRes int fillColorRes)
	{
		setFillColor(getContext().getResources().getColor(fillColorRes));
	}
	
	public int getBorderWidth()
	{
		return borderWidth;
	}
	
	public void setBorderWidth(int borderWidth)
	{
		if(borderWidth==this.borderWidth)
		{
			return;
		}
		
		this.borderWidth=borderWidth;
		setup();
	}
	
	public boolean isBorderOverlay()
	{
		return borderOverlay;
	}
	
	public void setBorderOverlay(boolean borderOverlay)
	{
		if(borderOverlay==this.borderOverlay)
		{
			return;
		}
		
		this.borderOverlay=borderOverlay;
		setup();
	}
	
	public boolean isDisableCircularTransformation()
	{
		return disableCircularTransformation;
	}
	
	public void setDisableCircularTransformation(boolean disableCircularTransformation)
	{
		if(this.disableCircularTransformation==disableCircularTransformation)
		{
			return;
		}
		
		this.disableCircularTransformation=disableCircularTransformation;
		initializeBitmap();
	}
	
	@Override
	public void setImageBitmap(Bitmap bm)
	{
		super.setImageBitmap(bm);
		initializeBitmap();
	}
	
	@Override
	public void setImageDrawable(Drawable drawable)
	{
		super.setImageDrawable(drawable);
		initializeBitmap();
	}
	
	@Override
	public void setImageResource(@DrawableRes int resId)
	{
		super.setImageResource(resId);
		initializeBitmap();
	}
	
	@Override
	public void setImageURI(Uri uri)
	{
		super.setImageURI(uri);
		initializeBitmap();
	}
	
	@Override
	public void setColorFilter(ColorFilter cf)
	{
		if(cf==colorFilter)
		{
			return;
		}
		
		colorFilter=cf;
		applyColorFilter();
		invalidate();
	}
	
	@Override
	public ColorFilter getColorFilter()
	{
		return colorFilter;
	}
	
	private void applyColorFilter()
	{
		if(bitmapPaint!=null)
		{
			bitmapPaint.setColorFilter(colorFilter);
		}
	}
	
	private Bitmap getBitmapFromDrawable(Drawable drawable)
	{
		if(drawable==null)
		{
			return null;
		}
		
		if(drawable instanceof BitmapDrawable)
		{
			return ((BitmapDrawable) drawable).getBitmap();
		}
		
		try
		{
			Bitmap bitmap;
			
			if(drawable instanceof ColorDrawable)
			{
				bitmap=Bitmap.createBitmap(COLOR_DRAWABLE_DIMENSION,COLOR_DRAWABLE_DIMENSION,BITMAP_CONFIG);
			}
			else
			{
				bitmap=Bitmap.createBitmap(drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight(),BITMAP_CONFIG);
			}
			
			Canvas canvas=new Canvas(bitmap);
			
			drawable.setBounds(0,0,canvas.getWidth(),canvas.getHeight());
			drawable.draw(canvas);
			return bitmap;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	private void initializeBitmap()
	{
		if(disableCircularTransformation)
		{
			bitmap=null;
		}
		else
		{
			bitmap=getBitmapFromDrawable(getDrawable());
		}
		
		setup();
	}
	
	private void setup()
	{
		if(!ready)
		{
			setupPending=true;
			return;
		}
		
		if(getWidth()==0 && getHeight()==0)
		{
			return;
		}
		
		if(bitmap==null)
		{
			invalidate();
			return;
		}
		
		bitmapShader=new BitmapShader(bitmap,Shader.TileMode.CLAMP,Shader.TileMode.CLAMP);
		
		bitmapPaint.setAntiAlias(true);
		bitmapPaint.setShader(bitmapShader);
		
		borderPaint.setStyle(Paint.Style.STROKE);
		borderPaint.setAntiAlias(true);
		borderPaint.setColor(borderColor);
		borderPaint.setStrokeWidth(borderWidth);
		
		fillPaint.setStyle(Paint.Style.FILL);
		fillPaint.setAntiAlias(true);
		fillPaint.setColor(fillColor);
		
		bitmapHeight=bitmap.getHeight();
		bitmapWidth=bitmap.getWidth();
		
		borderRect.set(calculateBounds());
		borderRadius=Math.min((borderRect.height()-borderWidth)/2.0f,(borderRect.width()-borderWidth)/2.0f);
		
		drawableRect.set(borderRect);
		if(!borderOverlay && borderWidth>0)
		{
			drawableRect.inset(borderWidth-1.0f,borderWidth-1.0f);
		}
		
		drawableRadius=Math.min(drawableRect.height()/2.0f,drawableRect.width()/2.0f);
		
		applyColorFilter();
		updateShaderMatrix();
		
		
		if(this.isEnableShadow())
		{
			//applyShadow();
		}
		
		invalidate();
	}
	
	private void applyShadow()
	{
		borderPaint.setShadowLayer(getShadowRadius(),getShadowDx(),getShadowDy(),getShadowColor());
		this.setLayerType(LAYER_TYPE_SOFTWARE,fillPaint);
	}
	
	public Bitmap eraseColor(Bitmap src,int color)
	{
		int width=src.getWidth();
		int height=src.getHeight();
		Bitmap b=src.copy(Bitmap.Config.ARGB_8888,true);
		b.setHasAlpha(true);
		
		int[] pixels=new int[width*height];
		src.getPixels(pixels,0,width,0,0,width,height);
		
		for(int i=0;i<width*height;i++)
		{
			if(pixels[i]==color)
			{
				pixels[i]=0;
			}
		}
		
		b.setPixels(pixels,0,width,0,0,width,height);
		
		return b;
	}
	
	public boolean isEnableShadow()
	{
		return enableShadow;
	}
	
	public ABPCircleImageView setEnableShadow(boolean enableShadow)
	{
		this.enableShadow=enableShadow;
		
		return this;
	}
	
	public int getShadowColor()
	{
		return shadowColor;
	}
	
	public ABPCircleImageView setShadowColor(int shadowColor)
	{
		this.shadowColor=shadowColor;
		
		return this;
	}
	
	public float getShadowRadius()
	{
		return shadowRadius;
	}
	
	public ABPCircleImageView setShadowRadius(float shadowRadius)
	{
		this.shadowRadius=shadowRadius;
		
		return this;
	}
	
	public float getShadowDx()
	{
		return shadowDx;
	}
	
	public ABPCircleImageView setShadowDx(float shadowDx)
	{
		this.shadowDx=shadowDx;
		
		return this;
	}
	
	public float getShadowDy()
	{
		return shadowDy;
	}
	
	public ABPCircleImageView setShadowDy(float shadowDy)
	{
		this.shadowDy=shadowDy;
		
		return this;
	}
	
	private int convertDpToPx(int dp)
	{
		return Math.round(dp*(getResources().getDisplayMetrics().xdpi/DisplayMetrics.DENSITY_DEFAULT));
	}
	
	private RectF calculateBounds()
	{
		int availableWidth=getWidth()-getPaddingLeft()-getPaddingRight();
		int availableHeight=getHeight()-getPaddingTop()-getPaddingBottom();
		
		if(isEnableShadow())
		{
			availableWidth-=convertDpToPx((int) getShadowDx());
			availableHeight-=convertDpToPx((int) getShadowDy());
		}
		
		int sideLength=Math.min(availableWidth,availableHeight);
		
		float left=getPaddingLeft()+(availableWidth-sideLength)/2f;
		float top=getPaddingTop()+(availableHeight-sideLength)/2f;
		
		return new RectF(left,top,left+sideLength,top+sideLength);
	}
	
	private void updateShaderMatrix()
	{
		float scale;
		float dx=0;
		float dy=0;
		
		shaderMatrix.set(null);
		
		if(bitmapWidth*drawableRect.height()>drawableRect.width()*bitmapHeight)
		{
			scale=drawableRect.height()/(float) bitmapHeight;
			dx=(drawableRect.width()-bitmapWidth*scale)*0.5f;
		}
		else
		{
			scale=drawableRect.width()/(float) bitmapWidth;
			dy=(drawableRect.height()-bitmapHeight*scale)*0.5f;
		}
		
		shaderMatrix.setScale(scale,scale);
		shaderMatrix.postTranslate((int) (dx+0.5f)+drawableRect.left,(int) (dy+0.5f)+drawableRect.top);
		
		bitmapShader.setLocalMatrix(shaderMatrix);
	}
}
