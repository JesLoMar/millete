import * as z from "zod";

const passwordSchema = z.string().min(8, "auth.form.passwordHint");

export const authFormFieldsSchema = z.object({
  identifier: z.string().optional(),
  usernameRegistro: z.string().optional(),
  emailRegistro: z.string().optional(),
  password: passwordSchema,
});

export type CombinedAuthFormData = z.infer<typeof authFormFieldsSchema>;

export const loginSchema = authFormFieldsSchema.refine(
  (data) => !!data.identifier?.trim(),
  {
    message: "auth.form.error.invalidCredentials",
    path: ["identifier"],
  }
);

export const registerSchema = authFormFieldsSchema
  .refine(
    (data) => !!data.usernameRegistro?.trim() || !!data.emailRegistro?.trim(),
    {
      message: "auth.form.registerHint",
      path: ["emailRegistro"],
    }
  )
  .refine(
    (data) => {
      if (!data.emailRegistro?.trim()) return true;
      const emailRegex = /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i;
      return emailRegex.test(data.emailRegistro);
    },
    {
      message: "auth.forgotPassword.emailInvalid",
      path: ["emailRegistro"],
    }
  );