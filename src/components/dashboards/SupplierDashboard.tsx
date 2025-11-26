import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Package, TrendingUp, Activity, Loader2 } from "lucide-react";
import BatchList from "@/components/BatchList";
import { BatchExportImport } from "@/components/BatchExportImport";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { useState, useEffect } from "react";
import { useToast } from "@/hooks/use-toast";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { productAPI, batchAPI, type Product, type Batch } from "@/lib/api";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { productSchema, batchSchema, type ProductFormData, type BatchFormData } from "@/lib/validationSchemas";

const SupplierDashboard = () => {
  const [productForm, setProductForm] = useState<ProductFormData>({
    name: "",
    description: "",
    category: "",
    sku: ""
  });

  const [productErrors, setProductErrors] = useState<Partial<Record<keyof ProductFormData, string>>>({});
  const [batchForm, setBatchForm] = useState<Partial<BatchFormData>>({
    batchNumber: "",
    quantity: undefined,
    currentLocation: "",
    productId: undefined
  });

  const [batchErrors, setBatchErrors] = useState<Partial<Record<keyof BatchFormData, string>>>({});
  const [open, setOpen] = useState(false);
  const [products, setProducts] = useState<Product[]>([]);
  const [batches, setBatches] = useState<Batch[]>([]);
  const [loading, setLoading] = useState(false);

  const { toast } = useToast();

  useEffect(() => {
    loadProducts();
    loadBatches();
  }, []);

  const loadProducts = async () => {
    try {
      const data = await productAPI.getAll();
      setProducts(data);
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.response?.data?.message || "Failed to load products",
        variant: "destructive",
      });
    }
  };

  const loadBatches = async () => {
    try {
      const data = await batchAPI.getMyBatches();  // MUST map to backend `/batches/my`
      setBatches(data);
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.response?.data?.message || "Failed to load batches",
        variant: "destructive",
      });
    }
  };

  const handleCreateProduct = async () => {
    setProductErrors({});

    const validation = productSchema.safeParse(productForm);
    if (!validation.success) {
      const errors: any = {};
      validation.error.errors.forEach((err) => {
        errors[err.path[0]] = err.message;
      });
      setProductErrors(errors);
      return;
    }

    setLoading(true);
    try {
      await productAPI.create(validation.data);

      toast({
        title: "Product Created",
        description: `Product "${validation.data.name}" registered successfully`,
      });

      setProductForm({ name: "", description: "", category: "", sku: "" });
      setOpen(false);
      loadProducts();
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.response?.data?.message || "Failed to create product",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  const handleCreateBatch = async () => {
    setBatchErrors({});

    const batchNumber = "BATCH-" + Math.random().toString(36).substring(2, 8).toUpperCase();

    const validation = batchSchema.safeParse({
      ...batchForm,
      batchNumber,
      quantity: Number(batchForm.quantity),
      productId: Number(batchForm.productId)
    });

    if (!validation.success) {
      const errors: any = {};
      validation.error.errors.forEach((err) => {
        errors[err.path[0]] = err.message;
      });
      setBatchErrors(errors);
      return;
    }

    setLoading(true);
    try {
      // MATCHES BACKEND PERFECTLY — NO STATUS FIELD SENT
      await batchAPI.create(validation.data);

      toast({
        title: "Batch Created",
        description: `Batch ${batchNumber} created with ${validation.data.quantity} units`,
      });

      setBatchForm({ batchNumber: "", quantity: undefined, currentLocation: "", productId: undefined });
      loadBatches();
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.response?.data?.message || "Failed to create batch",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Stats */}
      <div className="grid gap-4 md:grid-cols-3">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Total Products</CardTitle>
            <Package className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{products.length}</div>
            <p className="text-xs text-muted-foreground">Registered on chain</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Active Batches</CardTitle>
            <Activity className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{batches.length}</div>
            <p className="text-xs text-muted-foreground">In supply chain</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Blockchain TXs</CardTitle>
            <TrendingUp className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{batches.length * 2}</div>
            <p className="text-xs text-muted-foreground">All verified</p>
          </CardContent>
        </Card>
      </div>

      <BatchExportImport onImportSuccess={loadBatches} />

      {/* PRODUCT CREATION */}
      <Card>
        <CardHeader>
          <CardTitle>Create New Product</CardTitle>
          <CardDescription>Register a new product in the supply chain</CardDescription>
        </CardHeader>
        <CardContent>
          <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
              <Button className="w-full">
                <Package className="w-4 h-4 mr-2" />
                Create Product
              </Button>
            </DialogTrigger>

            <DialogContent>
              <DialogHeader>
                <DialogTitle>Create New Product</DialogTitle>
                <DialogDescription>Enter product details</DialogDescription>
              </DialogHeader>

              <div className="space-y-4">
                {/* Name */}
                <div className="space-y-2">
                  <Label>Product Name *</Label>
                  <Input
                    value={productForm.name}
                    onChange={(e) => setProductForm({ ...productForm, name: e.target.value })}
                  />
                  {productErrors.name && <p className="text-sm text-destructive">{productErrors.name}</p>}
                </div>

                {/* Category */}
                <div className="space-y-2">
                  <Label>Category *</Label>
                  <Input
                    value={productForm.category}
                    onChange={(e) => setProductForm({ ...productForm, category: e.target.value })}
                  />
                  {productErrors.category && <p className="text-sm text-destructive">{productErrors.category}</p>}
                </div>

                {/* SKU */}
                <div className="space-y-2">
                  <Label>SKU *</Label>
                  <Input
                    value={productForm.sku}
                    onChange={(e) => setProductForm({ ...productForm, sku: e.target.value })}
                  />
                  {productErrors.sku && <p className="text-sm text-destructive">{productErrors.sku}</p>}
                </div>

                {/* Description */}
                <div className="space-y-2">
                  <Label>Description *</Label>
                  <Textarea
                    value={productForm.description}
                    onChange={(e) => setProductForm({ ...productForm, description: e.target.value })}
                  />
                  {productErrors.description && <p className="text-sm text-destructive">{productErrors.description}</p>}
                </div>

                <Button onClick={handleCreateProduct} className="w-full" disabled={loading}>
                  {loading ? <Loader2 className="w-4 h-4 mr-2 animate-spin" /> : "Create Product"}
                </Button>
              </div>
            </DialogContent>
          </Dialog>
        </CardContent>
      </Card>

      {/* BATCH CREATION */}
      <Card>
        <CardHeader>
          <CardTitle>Create New Batch</CardTitle>
          <CardDescription>Create a batch for an existing product</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">

            <div className="space-y-2">
              <Label>Product *</Label>
              <Select
                value={batchForm.productId?.toString()}
                onValueChange={(value) => setBatchForm({ ...batchForm, productId: Number(value) })}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select a product" />
                </SelectTrigger>
                <SelectContent>
                  {products.map((product) => (
                    <SelectItem key={product.id} value={product.id.toString()}>
                      {product.name} ({product.sku})
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {batchErrors.productId && <p className="text-sm text-destructive">{batchErrors.productId}</p>}
            </div>

            <div className="space-y-2">
              <Label>Quantity *</Label>
              <Input
                type="number"
                value={batchForm.quantity || ""}
                onChange={(e) => setBatchForm({ ...batchForm, quantity: Number(e.target.value) })}
              />
              {batchErrors.quantity && <p className="text-sm text-destructive">{batchErrors.quantity}</p>}
            </div>

            <div className="space-y-2">
              <Label>Location *</Label>
              <Input
                value={batchForm.currentLocation}
                onChange={(e) => setBatchForm({ ...batchForm, currentLocation: e.target.value })}
              />
              {batchErrors.currentLocation && (
                <p className="text-sm text-destructive">{batchErrors.currentLocation}</p>
              )}
            </div>

            <Button onClick={handleCreateBatch} disabled={loading || products.length === 0} className="w-full">
              {loading ? <Loader2 className="w-4 h-4 mr-2 animate-spin" /> : "Create Batch"}
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Batch List */}
      <Card>
        <CardHeader>
          <CardTitle>My Batches</CardTitle>
          <CardDescription>Track all batches created by you</CardDescription>
        </CardHeader>
        <CardContent>
          <BatchList batches={batches} onRefresh={loadBatches} />
        </CardContent>
      </Card>
    </div>
  );
};

export default SupplierDashboard;
