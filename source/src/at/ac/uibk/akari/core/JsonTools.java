package at.ac.uibk.akari.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;

import at.ac.uibk.akari.core.annotations.JsonIgnorePermanent;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class JsonTools {

	private static JsonTools singleton;

	private Gson permanentSerializer;
	private Gson cachedSerializer;

	private JsonTools() {
		GsonBuilder builder = new GsonBuilder();
		builder.setExclusionStrategies(new ExclusionStrategy() {

			@Override
			public boolean shouldSkipField(final FieldAttributes fileAttr) {
				for (Annotation annotation : fileAttr.getAnnotations()) {
					if (annotation.annotationType().equals(JsonIgnorePermanent.class)) {
						return true;
					}
				}
				return false;
			}

			@Override
			public boolean shouldSkipClass(final Class<?> c) {
				// TODO Auto-generated method stub
				return false;
			}
		});

		this.permanentSerializer = builder.create();
		this.cachedSerializer = new Gson();

	}

	public static JsonTools getInstance() {
		if (JsonTools.singleton == null) {
			JsonTools.singleton = new JsonTools();
		}
		return JsonTools.singleton;
	}

	public String toJson(final Object src, final boolean permanent) {
		if (permanent) {
			return this.cachedSerializer.toJson(src);
		} else {
			return this.permanentSerializer.toJson(src);
		}
	}

	public <T> T fromJson(final Class<T> t, final String jsonObject) {
		return this.permanentSerializer.fromJson(jsonObject, t);
	}

	public <T> T fromJson(final Class<T> t, final File file) throws JsonSyntaxException, JsonIOException, FileNotFoundException {
		return this.permanentSerializer.fromJson(new FileReader(file), t);
	}

	public <T> T fromJson(final Class<T> t, final InputStream inputStream) throws JsonSyntaxException, JsonIOException, FileNotFoundException {
		return this.permanentSerializer.fromJson(new InputStreamReader(inputStream), t);
	}

}
