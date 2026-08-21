import * as z from "zod";

export const PASSWORD_MIN_LENGTH = 8;

export const passwordSchema = z.string().min(PASSWORD_MIN_LENGTH, { message: "validations:min_length" });

const authFormFieldsSchema = z.object({
  identifier: z.string().optional(),
  usernameRegistro: z.string().optional(),
  emailRegistro: z.string().optional(),
  password: passwordSchema,
});

export type CombinedAuthFormData = z.infer<typeof authFormFieldsSchema>;

export const loginSchema = authFormFieldsSchema.refine(
  (data) => !!data.identifier?.trim(),
  {
    message: "validations:required",
    path: ["identifier"],
  }
);

export const registerSchema = authFormFieldsSchema
  .refine(
    (data) => !!data.usernameRegistro?.trim() || !!data.emailRegistro?.trim(),
    {
      message: "auth.form.error.usernameRequired",
      path: ["usernameRegistro"],
    }
  )
  .refine(
    (data) => {
      if (!data.emailRegistro?.trim()) return true;
      const emailRegex = /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i;
      return emailRegex.test(data.emailRegistro);
    },
    {
      message: "validations:invalid_email",
      path: ["emailRegistro"],
    }
  );